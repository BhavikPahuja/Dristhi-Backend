package com.project.tekathon.drishti.service;

import com.project.tekathon.drishti.dto.PersonDtos.PersonConnectionResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonProfileResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonSearchResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonTimelineItemResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.LocationResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.SurveillanceResponse;
import com.project.tekathon.drishti.entity.CdrEntity;
import com.project.tekathon.drishti.entity.InvestigationEventEntity;
import com.project.tekathon.drishti.entity.LocationEntity;
import com.project.tekathon.drishti.entity.PersonEntity;
import com.project.tekathon.drishti.entity.PhoneEntity;
import com.project.tekathon.drishti.entity.RelationshipEntity;
import com.project.tekathon.drishti.entity.SurveillanceReportEntity;
import com.project.tekathon.drishti.entity.TransactionEntity;
import com.project.tekathon.drishti.entity.VehicleEntity;
import com.project.tekathon.drishti.exception.ResourceNotFoundException;
import com.project.tekathon.drishti.repository.CdrRepository;
import com.project.tekathon.drishti.repository.LocationRepository;
import com.project.tekathon.drishti.repository.PersonRepository;
import com.project.tekathon.drishti.repository.PhoneRepository;
import com.project.tekathon.drishti.repository.RelationshipRepository;
import com.project.tekathon.drishti.repository.SurveillanceRepository;
import com.project.tekathon.drishti.repository.TransactionRepository;
import com.project.tekathon.drishti.repository.VehicleRepository;
import com.project.tekathon.drishti.util.IdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final PhoneRepository phoneRepository;
    private final VehicleRepository vehicleRepository;
    private final TransactionRepository transactionRepository;
    private final RelationshipRepository relationshipRepository;
    private final CdrRepository cdrRepository;
    private final LocationRepository locationRepository;
    private final SurveillanceRepository surveillanceRepository;
    private final NetworkService networkService;

    public PersonResponse create(PersonEntity entity) {
        long next = personRepository.count() + 1;
        entity.setPersonId(IdGenerator.next("P", next));
        PersonEntity saved = personRepository.save(entity);
        networkService.syncPerson(saved);
        return toResponse(saved);
    }

    public PersonResponse update(PersonEntity entity) {
        PersonEntity saved = personRepository.save(entity);
        networkService.syncPerson(saved);
        return toResponse(saved);
    }

    public List<PersonResponse> listAll() {
        return personRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<PersonSearchResponse> search(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return personRepository.findAll().stream()
                .filter(person -> matches(person, normalized))
                .map(person -> new PersonSearchResponse(person.getPersonId(), person.getName(), person.getAliases(), score(person, normalized)))
                .toList();
    }

    public PersonResponse get(String personId) {
        return toResponse(findPerson(personId));
    }

    public PersonProfileResponse profile(String personId) {
        PersonEntity person = findPerson(personId);
        List<String> phones = phoneRepository.findByOwnerPersonId(personId).stream().map(PhoneEntity::getPhoneId).toList();
        List<String> vehicles = vehicleRepository.findByOwnerPersonId(personId).stream().map(VehicleEntity::getVehicleId).toList();
        List<String> accounts = relationshipRepository.findAll().stream()
                .filter(relationship -> personId.equals(relationship.getSourceId()) && "OWNS".equalsIgnoreCase(relationship.getRelationship()))
                .map(RelationshipEntity::getTargetId)
                .toList();
        List<String> relatedCases = relationshipRepository.findAll().stream()
                .filter(relationship -> personId.equals(relationship.getSourceId()) || personId.equals(relationship.getTargetId()))
                .map(RelationshipEntity::getCaseId)
                .distinct()
                .toList();
        return new PersonProfileResponse(person.getPersonId(), person.getName(), person.getAliases(), person.getAge(),
                person.getDateOfBirth(), person.getAddress(), phones, vehicles, accounts, relatedCases);
    }

    public List<PersonConnectionResponse> connections(String personId) {
        findPerson(personId);
        return relationshipRepository.findAll().stream()
                .filter(relationship -> personId.equals(relationship.getSourceId()) || personId.equals(relationship.getTargetId()))
                .map(relationship -> new PersonConnectionResponse(
                        relationship.getSourceId(),
                        relationship.getRelationship(),
                        relationship.getTargetId(),
                        relationship.getConfidence(),
                        relationship.getFirstObserved(),
                        relationship.getLastObserved()))
                .toList();
    }

    public List<PersonTimelineItemResponse> timeline(String personId) {
        findPerson(personId);
        List<PersonTimelineItemResponse> timeline = new ArrayList<>();
        List<String> phoneIds = phoneRepository.findByOwnerPersonId(personId).stream().map(PhoneEntity::getPhoneId).toList();
        for (String phoneId : phoneIds) {
            for (CdrEntity cdr : cdrRepository.findByCallerPhoneIdOrReceiverPhoneIdOrderByTimestampDesc(phoneId, phoneId)) {
                timeline.add(new PersonTimelineItemResponse(cdr.getCdrId(), "CALL", cdr.getTimestamp(),
                        "Call involving " + phoneId, cdr.getCdrId()));
            }
        }
        for (TransactionEntity tx : transactions(personId)) {
            timeline.add(new PersonTimelineItemResponse(tx.getTransactionId(), "TRANSACTION", tx.getTimestamp(),
                    tx.getDescription(), tx.getTransactionId()));
        }
        for (RelationshipEntity relationship : relationshipRepository.findAll().stream()
                .filter(r -> personId.equals(r.getSourceId()) || personId.equals(r.getTargetId()))
                .toList()) {
            timeline.add(new PersonTimelineItemResponse(
                    relationship.getRelationshipId(),
                    relationship.getRelationship(),
                    relationship.getCreatedAt(),
                    relationship.getSupportText(),
                    null));
        }
        return timeline.stream()
                .sorted(Comparator.comparing(PersonTimelineItemResponse::timestamp, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<CdrEntity> calls(String personId) {
        List<String> phoneIds = phoneRepository.findByOwnerPersonId(personId).stream().map(PhoneEntity::getPhoneId).toList();
        return cdrRepository.findAll().stream()
                .filter(cdr -> phoneIds.contains(cdr.getCallerPhoneId()) || phoneIds.contains(cdr.getReceiverPhoneId()))
                .sorted(Comparator.comparing(CdrEntity::getTimestamp).reversed())
                .toList();
    }

    public List<TransactionEntity> transactions(String personId) {
        List<String> accountIds = relationshipRepository.findAll().stream()
                .filter(relationship -> personId.equals(relationship.getSourceId()) && "OWNS".equalsIgnoreCase(relationship.getRelationship()))
                .map(RelationshipEntity::getTargetId)
                .toList();
        return transactionRepository.findAll().stream()
                .filter(tx -> accountIds.contains(tx.getSourceAccountId()) || accountIds.contains(tx.getDestinationAccountId()))
                .sorted(Comparator.comparing(TransactionEntity::getTimestamp).reversed())
                .toList();
    }

    public List<VehicleEntity> vehicles(String personId) {
        return vehicleRepository.findByOwnerPersonId(personId);
    }

    public List<LocationResponse> locations(String personId) {
        return surveillanceRepository.findByPersonIdOrderByTimestampDesc(personId).stream()
                .map(report -> report.getLocationId())
                .filter(Objects::nonNull)
                .distinct()
                .map(locationRepository::findById)
                .flatMap(java.util.Optional::stream)
                .map(location -> new LocationResponse(location.getLocationId(), location.getName(), location.getAddress(), location.getLatitude(), location.getLongitude()))
                .toList();
    }

    public List<SurveillanceResponse> surveillance(String personId) {
        return surveillanceRepository.findByPersonIdOrderByTimestampDesc(personId).stream()
                .map(report -> new SurveillanceResponse(report.getReportId(), report.getCaseId(), report.getDescription(),
                        report.getTimestamp(), report.getLocationId(), report.getVehicleId(), report.getPersonId(), report.getSource()))
                .toList();
    }

    public PersonEntity findPerson(String personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person " + personId + " not found"));
    }

    private boolean matches(PersonEntity person, String query) {
        if (query.isBlank()) {
            return false;
        }
        if (person.getName() != null && person.getName().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        return person.getAliases() != null && person.getAliases().stream()
                .filter(Objects::nonNull)
                .anyMatch(alias -> alias.toLowerCase(Locale.ROOT).contains(query));
    }

    private double score(PersonEntity person, String query) {
        if (person.getName() != null && person.getName().equalsIgnoreCase(query)) {
            return 0.98;
        }
        return 0.82;
    }

    private PersonResponse toResponse(PersonEntity entity) {
        return new PersonResponse(entity.getPersonId(), entity.getName(), entity.getAliases(), entity.getAge(),
                entity.getDateOfBirth(), entity.getAddress(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
