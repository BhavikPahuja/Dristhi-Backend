package com.project.tekathon.drishti.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tekathon.drishti.dto.InvestigationDtos.CdrResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateCdrRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateEvidenceRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateEventRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateLocationRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateSurveillanceRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateTransactionRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateVehicleRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.EvidenceResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.EventResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.LocationResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.SurveillanceResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.TransactionResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.VehicleResponse;
import com.project.tekathon.drishti.entity.AccountEntity;
import com.project.tekathon.drishti.entity.CdrEntity;
import com.project.tekathon.drishti.entity.EvidenceEntity;
import com.project.tekathon.drishti.entity.InvestigationEventEntity;
import com.project.tekathon.drishti.entity.LocationEntity;
import com.project.tekathon.drishti.entity.SurveillanceReportEntity;
import com.project.tekathon.drishti.entity.PersonEntity;
import com.project.tekathon.drishti.entity.TransactionEntity;
import com.project.tekathon.drishti.entity.VehicleEntity;
import com.project.tekathon.drishti.exception.ResourceNotFoundException;
import com.project.tekathon.drishti.repository.AccountRepository;
import com.project.tekathon.drishti.repository.CdrRepository;
import com.project.tekathon.drishti.repository.EvidenceRepository;
import com.project.tekathon.drishti.repository.EventRepository;
import com.project.tekathon.drishti.repository.LocationRepository;
import com.project.tekathon.drishti.repository.PersonRepository;
import com.project.tekathon.drishti.repository.PhoneRepository;
import com.project.tekathon.drishti.repository.RelationshipRepository;
import com.project.tekathon.drishti.repository.SurveillanceRepository;
import com.project.tekathon.drishti.repository.TransactionRepository;
import com.project.tekathon.drishti.repository.VehicleRepository;
import com.project.tekathon.drishti.util.IdGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestigationDataService {

    private final CdrRepository cdrRepository;
    private final TransactionRepository transactionRepository;
    private final VehicleRepository vehicleRepository;
    private final LocationRepository locationRepository;
    private final SurveillanceRepository surveillanceRepository;
    private final EvidenceRepository evidenceRepository;
    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;
    private final RelationshipRepository relationshipRepository;
    private final PhoneRepository phoneRepository;
    private final PersonRepository personRepository;
    private final NetworkService networkService;
    private final ObjectMapper objectMapper;

    public CdrResponse createCdr(CreateCdrRequest request) {
        CdrEntity entity = CdrEntity.builder()
                .cdrId(IdGenerator.next("CDR", cdrRepository.count() + 1))
                .callerPhoneId(request.callerPhoneId())
                .receiverPhoneId(request.receiverPhoneId())
                .timestamp(request.timestamp())
                .durationSeconds(request.durationSeconds())
                .caseId(request.caseId())
                .build();
        CdrEntity saved = cdrRepository.save(entity);
        syncCall(saved);
        return toResponse(saved);
    }

    public List<CdrResponse> bulkCdr(List<CreateCdrRequest> requests) {
        List<CdrResponse> responses = new ArrayList<>();
        for (CreateCdrRequest request : requests) {
            responses.add(createCdr(request));
        }
        return responses;
    }

    public CdrResponse getCdr(String cdrId) {
        return toResponse(findCdr(cdrId));
    }

    public List<CdrResponse> phoneCalls(String phoneId) {
        phoneRepository.findById(phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Phone " + phoneId + " not found"));
        return cdrRepository.findByCallerPhoneIdOrReceiverPhoneIdOrderByTimestampDesc(phoneId, phoneId).stream()
                .map(this::toResponse)
                .toList();
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        TransactionEntity entity = TransactionEntity.builder()
                .transactionId(IdGenerator.next("TX", transactionRepository.count() + 1))
                .sourceAccountId(request.sourceAccountId())
                .destinationAccountId(request.destinationAccountId())
                .amount(request.amount())
                .currency(request.currency())
                .timestamp(request.timestamp())
                .description(request.description())
                .caseId(request.caseId())
                .build();
        TransactionEntity saved = transactionRepository.save(entity);
        syncTransaction(saved);
        return toResponse(saved);
    }

    public List<TransactionResponse> bulkTransactions(List<CreateTransactionRequest> requests) {
        List<TransactionResponse> responses = new ArrayList<>();
        for (CreateTransactionRequest request : requests) {
            responses.add(createTransaction(request));
        }
        return responses;
    }

    public TransactionResponse getTransaction(String transactionId) {
        TransactionEntity entity = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction " + transactionId + " not found"));
        return toResponse(entity);
    }

    public List<TransactionResponse> accountTransactions(String accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account " + accountId + " not found"));
        return transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByTimestampDesc(accountId, accountId).stream()
                .map(this::toResponse)
                .toList();
    }

    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        VehicleEntity entity = VehicleEntity.builder()
                .vehicleId(IdGenerator.next("V", vehicleRepository.count() + 1))
                .registrationNumber(request.registrationNumber())
                .make(request.make())
                .model(request.model())
                .ownerPersonId(request.ownerPersonId())
                .build();
        VehicleEntity saved = vehicleRepository.save(entity);
        networkService.syncVehicle(saved);
        return toResponse(saved);
    }

    public List<VehicleResponse> listVehicles() {
        return vehicleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public VehicleResponse getVehicle(String vehicleId) {
        return toResponse(findVehicle(vehicleId));
    }

    public List<VehicleResponse> searchVehicle(String registration) {
        return vehicleRepository.findByRegistrationNumber(registration).stream().map(this::toResponse).toList();
    }

    public List<Object> vehicleHistory(String vehicleId) {
        List<Object> history = new ArrayList<>();
        surveillanceRepository.findByVehicleIdOrderByTimestampDesc(vehicleId).forEach(history::add);
        eventRepository.findAll().stream()
                .filter(event -> vehicleId.equals(event.getEntityId()))
                .forEach(history::add);
        return history;
    }

    public List<VehicleResponse> personVehicles(String personId) {
        return vehicleRepository.findByOwnerPersonId(personId).stream().map(this::toResponse).toList();
    }

    public LocationResponse createLocation(CreateLocationRequest request) {
        LocationEntity entity = LocationEntity.builder()
                .locationId(IdGenerator.next("LOC", locationRepository.count() + 1))
                .name(request.name())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();
        LocationEntity saved = locationRepository.save(entity);
        networkService.syncLocation(saved);
        return toResponse(saved);
    }

    public LocationResponse getLocation(String locationId) {
        return toResponse(findLocation(locationId));
    }

    public EventResponse createLocationEvent(String personId, String locationId, Instant timestamp, String source) {
        return createEvent(new CreateEventRequest("CASE-UNKNOWN", "VISIT", timestamp, locationId, source, null, personId));
    }

    public List<LocationResponse> personLocations(String personId) {
        return surveillanceRepository.findByPersonIdOrderByTimestampDesc(personId).stream().map(SurveillanceReportEntity::getLocationId)
                .distinct()
                .map(locationRepository::findById)
                .flatMap(java.util.Optional::stream)
                .map(this::toResponse)
                .toList();
    }

    public List<SurveillanceResponse> locationActivity(String locationId) {
        return surveillanceRepository.findAll().stream()
                .filter(report -> locationId.equals(report.getLocationId()))
                .sorted(Comparator.comparing(SurveillanceReportEntity::getTimestamp).reversed())
                .map(this::toResponse)
                .toList();
    }

    public List<String> personsAtLocation(String locationId) {
        return surveillanceRepository.findAll().stream()
                .filter(report -> locationId.equals(report.getLocationId()) && report.getPersonId() != null)
                .map(SurveillanceReportEntity::getPersonId)
                .distinct()
                .toList();
    }

    public SurveillanceResponse createSurveillance(CreateSurveillanceRequest request) {
        SurveillanceReportEntity entity = SurveillanceReportEntity.builder()
                .reportId(IdGenerator.next("REP", surveillanceRepository.count() + 1))
                .caseId(request.caseId())
                .description(request.description())
                .timestamp(request.timestamp())
                .locationId(request.locationId())
                .vehicleId(request.vehicleId())
                .personId(request.personId())
                .source(request.source())
                .build();
        SurveillanceReportEntity saved = surveillanceRepository.save(entity);
        return toResponse(saved);
    }

    public SurveillanceResponse getSurveillance(String reportId) {
        return toResponse(surveillanceRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Surveillance report " + reportId + " not found")));
    }

    public List<SurveillanceResponse> vehicleSurveillance(String vehicleId) {
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle " + vehicleId + " not found"));
        return surveillanceRepository.findByVehicleIdOrderByTimestampDesc(vehicleId).stream()
                .map(this::toResponse)
                .toList();
    }

    public EvidenceResponse createEvidence(CreateEvidenceRequest request) {
        EvidenceEntity entity = EvidenceEntity.builder()
                .evidenceId(IdGenerator.next("E", evidenceRepository.count() + 1))
                .caseId(request.caseId())
                .type(request.type())
                .description(request.description())
                .sourceDocumentId(request.sourceDocumentId())
                .entityId(request.entityId())
                .relationshipId(request.relationshipId())
                .sourceSpanText(request.sourceSpanText())
                .build();
        return toResponse(evidenceRepository.save(entity));
    }

    public EvidenceResponse getEvidence(String evidenceId) {
        return toResponse(evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence " + evidenceId + " not found")));
    }

    public List<EvidenceResponse> caseEvidence(String caseId) {
        return evidenceRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream().map(this::toResponse).toList();
    }

    public List<EvidenceResponse> entityEvidence(String entityId) {
        return evidenceRepository.findByEntityIdOrderByCreatedAtDesc(entityId).stream().map(this::toResponse).toList();
    }

    public List<EvidenceResponse> relationshipEvidence(String relationshipId) {
        return evidenceRepository.findByRelationshipIdOrderByCreatedAtDesc(relationshipId).stream().map(this::toResponse).toList();
    }

    public EventResponse createEvent(CreateEventRequest request) {
        InvestigationEventEntity entity = InvestigationEventEntity.builder()
                .eventId(IdGenerator.next("EV", eventRepository.count() + 1))
                .caseId(request.caseId())
                .eventType(request.eventType())
                .timestamp(request.timestamp())
                .locationId(request.locationId())
                .description(request.description())
                .sourceDocumentId(request.sourceDocumentId())
                .entityId(request.entityId())
                .source(null)
                .build();
        return toResponse(eventRepository.save(entity));
    }

    public List<EventResponse> caseEvents(String caseId) {
        return eventRepository.findByCaseIdOrderByTimestampDesc(caseId).stream().map(this::toResponse).toList();
    }

    public void applyNlpExtraction(String caseId, String documentId, List<com.project.tekathon.drishti.dto.IntegrationDtos.NlpEntityResult> entities,
            List<com.project.tekathon.drishti.dto.IntegrationDtos.NlpRelationshipResult> relationships,
            List<com.project.tekathon.drishti.dto.IntegrationDtos.NlpEventResult> events) {
        
        Map<String, String> idMapping = new HashMap<>();

        for (var entity : entities) {
            if ("PERSON".equalsIgnoreCase(entity.type()) && entity.canonicalId() != null) {
                String mentionName = entity.mention() == null ? "" : entity.mention().trim();

                // Deduplicate by matching person name (case-insensitive) or existing ID
                var existingPerson = personRepository.findAll().stream()
                        .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(mentionName))
                        .findFirst();

                String resolvedId;
                if (existingPerson.isPresent()) {
                    resolvedId = existingPerson.get().getPersonId();
                } else if (personRepository.findById(entity.canonicalId()).isPresent()) {
                    resolvedId = entity.canonicalId();
                } else {
                    resolvedId = IdGenerator.next("P", personRepository.count() + 1);
                    PersonEntity person = PersonEntity.builder()
                            .personId(resolvedId)
                            .name(mentionName)
                            .status("EXTRACTED_PERSON")
                            .notes("Extracted from document " + documentId + " (canonical: " + entity.canonicalId() + ")")
                            .build();
                    PersonEntity savedPerson = personRepository.save(person);
                    try {
                        networkService.syncPerson(savedPerson);
                    } catch (Exception ex) {
                        log.warn("Failed to sync extracted person {}", savedPerson.getPersonId(), ex);
                    }
                }

                idMapping.put(entity.canonicalId(), resolvedId);

                createEvidence(new CreateEvidenceRequest(caseId, "ENTITY", "Extracted person mention " + entity.mention(), documentId,
                        resolvedId, null, entity.evidence() == null ? null : entity.evidence().text()));
            }
        }
        for (var relationship : relationships) {
            String sourceId = idMapping.getOrDefault(relationship.sourceId(), relationship.sourceId());
            String targetId = idMapping.getOrDefault(relationship.targetId(), relationship.targetId());
            com.project.tekathon.drishti.dto.IntegrationDtos.NlpRelationshipResult mappedRel =
                    new com.project.tekathon.drishti.dto.IntegrationDtos.NlpRelationshipResult(
                            sourceId, relationship.relation(), targetId, relationship.confidence(), relationship.evidence());
            RelationshipEntityBuilder.create(relationshipRepository, networkService, objectMapper, caseId, documentId, mappedRel);
        }
        for (var event : events) {
            String primaryParticipant = null;
            if (event.participants() != null && !event.participants().isEmpty()) {
                String rawId = event.participants().get(0);
                primaryParticipant = idMapping.getOrDefault(rawId, rawId);
            }
            createEvent(new CreateEventRequest(caseId, event.type(), event.date() == null ? Instant.now() : event.date().atStartOfDay(java.time.ZoneOffset.UTC).toInstant(),
                    event.locationId(), "Extracted event", documentId, primaryParticipant));
        }
    }

    public CdrEntity findCdr(String cdrId) {
        return cdrRepository.findById(cdrId)
                .orElseThrow(() -> new ResourceNotFoundException("CDR " + cdrId + " not found"));
    }

    public VehicleEntity findVehicle(String vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle " + vehicleId + " not found"));
    }

    public LocationEntity findLocation(String locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location " + locationId + " not found"));
    }

    private void syncCall(CdrEntity saved) {
        try {
            networkService.syncRelationship(com.project.tekathon.drishti.entity.RelationshipEntity.builder()
                    .relationshipId(IdGenerator.next("REL", relationshipRepository.count() + 1))
                    .caseId(saved.getCaseId())
                    .sourceId(saved.getCallerPhoneId())
                    .targetId(saved.getReceiverPhoneId())
                    .relationship("CALLED")
                    .confidence(0.99)
                    .firstObserved(saved.getTimestamp())
                    .lastObserved(saved.getTimestamp())
                    .supportText("CDR")
                    .evidenceIdsJson("[]")
                    .sourceDocumentIdsJson("[]")
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to sync call {}", saved.getCdrId(), ex);
        }
    }

    private void syncTransaction(TransactionEntity saved) {
        try {
            networkService.syncRelationship(com.project.tekathon.drishti.entity.RelationshipEntity.builder()
                    .relationshipId(IdGenerator.next("REL", relationshipRepository.count() + 1))
                    .caseId(saved.getCaseId())
                    .sourceId(saved.getSourceAccountId())
                    .targetId(saved.getDestinationAccountId())
                    .relationship("TRANSFERRED_TO")
                    .confidence(0.99)
                    .firstObserved(saved.getTimestamp())
                    .lastObserved(saved.getTimestamp())
                    .supportText(saved.getDescription())
                    .evidenceIdsJson("[]")
                    .sourceDocumentIdsJson("[]")
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to sync transaction {}", saved.getTransactionId(), ex);
        }
    }

    private CdrResponse toResponse(CdrEntity entity) {
        return new CdrResponse(entity.getCdrId(), entity.getCallerPhoneId(), entity.getReceiverPhoneId(),
                entity.getTimestamp(), entity.getDurationSeconds(), entity.getCaseId());
    }

    private TransactionResponse toResponse(TransactionEntity entity) {
        return new TransactionResponse(entity.getTransactionId(), entity.getSourceAccountId(), entity.getDestinationAccountId(),
                entity.getAmount(), entity.getCurrency(), entity.getTimestamp(), entity.getDescription(), entity.getCaseId());
    }

    private VehicleResponse toResponse(VehicleEntity entity) {
        return new VehicleResponse(entity.getVehicleId(), entity.getRegistrationNumber(), entity.getMake(), entity.getModel(), entity.getOwnerPersonId());
    }

    private LocationResponse toResponse(LocationEntity entity) {
        return new LocationResponse(entity.getLocationId(), entity.getName(), entity.getAddress(), entity.getLatitude(), entity.getLongitude());
    }

    private SurveillanceResponse toResponse(SurveillanceReportEntity entity) {
        return new SurveillanceResponse(entity.getReportId(), entity.getCaseId(), entity.getDescription(),
                entity.getTimestamp(), entity.getLocationId(), entity.getVehicleId(), entity.getPersonId(), entity.getSource());
    }

    private EvidenceResponse toResponse(EvidenceEntity entity) {
        return new EvidenceResponse(entity.getEvidenceId(), entity.getCaseId(), entity.getType(), entity.getDescription(),
                entity.getSourceDocumentId(), entity.getEntityId(), entity.getRelationshipId(), entity.getSourceSpanText());
    }

    private EventResponse toResponse(InvestigationEventEntity entity) {
        return new EventResponse(entity.getEventId(), entity.getCaseId(), entity.getEventType(), entity.getTimestamp(),
                entity.getLocationId(), entity.getDescription(), entity.getSourceDocumentId(), entity.getEntityId(), entity.getSource());
    }

    /**
     * Small helper to keep relationship creation in one place when NLP output creates new edges.
     */
    static final class RelationshipEntityBuilder {
        private RelationshipEntityBuilder() {
        }

        static void create(RelationshipRepository relationshipRepository, NetworkService networkService, ObjectMapper objectMapper,
                String caseId, String documentId, com.project.tekathon.drishti.dto.IntegrationDtos.NlpRelationshipResult relationship) {
            try {
                String relationshipId = IdGenerator.next("REL", relationshipRepository.count() + 1);
                com.project.tekathon.drishti.entity.RelationshipEntity entity = com.project.tekathon.drishti.entity.RelationshipEntity.builder()
                        .relationshipId(relationshipId)
                        .caseId(caseId)
                        .sourceId(relationship.sourceId())
                        .targetId(relationship.targetId())
                        .relationship(relationship.relation())
                        .confidence(relationship.confidence())
                        .firstObserved(Instant.now())
                        .lastObserved(Instant.now())
                        .supportText(relationship.evidence() == null ? null : relationship.evidence().toString())
                        .evidenceIdsJson("[]")
                        .sourceDocumentIdsJson(writeJson(objectMapper, List.of(documentId)))
                        .build();
                relationshipRepository.save(entity);
                networkService.syncRelationship(entity);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to create relationship from NLP output", ex);
            }
        }

        private static String writeJson(ObjectMapper objectMapper, Object value) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException ex) {
                return "[]";
            }
        }
    }
}
