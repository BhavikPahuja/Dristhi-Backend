package com.project.tekathon.drishti.service;

import com.project.tekathon.drishti.dto.CaseDtos.CaseResponse;
import com.project.tekathon.drishti.dto.DocumentDtos.DocumentResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonTimelineItemResponse;
import com.project.tekathon.drishti.entity.AccountEntity;
import com.project.tekathon.drishti.entity.CaseEntity;
import com.project.tekathon.drishti.entity.DocumentEntity;
import com.project.tekathon.drishti.entity.LocationEntity;
import com.project.tekathon.drishti.entity.PersonEntity;
import com.project.tekathon.drishti.entity.PhoneEntity;
import com.project.tekathon.drishti.entity.VehicleEntity;
import com.project.tekathon.drishti.exception.ResourceNotFoundException;
import com.project.tekathon.drishti.repository.AccountRepository;
import com.project.tekathon.drishti.repository.CaseRepository;
import com.project.tekathon.drishti.repository.DocumentRepository;
import com.project.tekathon.drishti.repository.LocationRepository;
import com.project.tekathon.drishti.repository.PersonRepository;
import com.project.tekathon.drishti.repository.PhoneRepository;
import com.project.tekathon.drishti.repository.VehicleRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntityLookupService {

    private final PersonService personService;
    private final CaseService caseService;
    private final DocumentService documentService;
    private final InvestigationDataService investigationDataService;
    private final NetworkService networkService;
    private final PersonRepository personRepository;
    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;
    private final PhoneRepository phoneRepository;
    private final VehicleRepository vehicleRepository;
    private final AccountRepository accountRepository;
    private final LocationRepository locationRepository;

    public Map<String, Object> get(String entityId) {
        String type = entityType(entityId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entityId", entityId);
        result.put("entityType", type);
        switch (type) {
            case "PERSON" -> {
                PersonEntity person = personService.findPerson(entityId);
                result.put("data", personService.profile(entityId));
            }
            case "CASE" -> {
                CaseEntity caseEntity = caseService.findCase(entityId);
                result.put("data", new CaseResponse(caseEntity.getCaseId(), caseEntity.getTitle(), caseEntity.getDescription(),
                        caseEntity.getStatus(), caseEntity.getCreatedAt(), caseEntity.getUpdatedAt()));
            }
            case "DOCUMENT" -> {
                DocumentResponse document = documentService.get(entityId);
                result.put("data", document);
            }
            case "PHONE" -> result.put("data", phoneRepository.findById(entityId).orElse(null));
            case "VEHICLE" -> result.put("data", vehicleRepository.findById(entityId).orElse(null));
            case "ACCOUNT" -> result.put("data", accountRepository.findById(entityId).orElse(null));
            case "LOCATION" -> result.put("data", locationRepository.findById(entityId).orElse(null));
            default -> {
                result.put("data", Map.of("entityId", entityId));
            }
        }
        return result;
    }

    public Object connections(String entityId, int depth) {
        return networkService.getEntityNetwork(entityId, depth);
    }

    public List<PersonTimelineItemResponse> timeline(String entityId) {
        String type = entityType(entityId);
        return switch (type) {
            case "PERSON" -> personService.timeline(entityId);
            case "CASE" -> caseService.timeline(entityId);
            default -> List.of();
        };
    }

    public List<Map<String, Object>> search(String query) {
        List<Map<String, Object>> results = new java.util.ArrayList<>();
        for (var person : personService.search(query)) {
            results.add(Map.of("entityId", person.personId(), "entityType", "PERSON", "label", person.name(), "confidence", person.confidence()));
        }
        for (CaseEntity caseEntity : caseRepository.findAll()) {
            if (caseEntity.getTitle() != null && caseEntity.getTitle().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                results.add(Map.of("entityId", caseEntity.getCaseId(), "entityType", "CASE", "label", caseEntity.getTitle(), "confidence", 0.95));
            }
        }
        for (DocumentEntity document : documentRepository.findAll()) {
            if (document.getTitle() != null && document.getTitle().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                results.add(Map.of("entityId", document.getDocumentId(), "entityType", "DOCUMENT", "label", document.getTitle(), "confidence", 0.9));
            }
        }
        return results;
    }

    private String entityType(String entityId) {
        if (entityId == null) {
            return "ENTITY";
        }
        String upper = entityId.toUpperCase(Locale.ROOT);
        if (upper.startsWith("CASE")) {
            return "CASE";
        }
        if (upper.startsWith("DOC")) {
            return "DOCUMENT";
        }
        if (upper.startsWith("PH")) {
            return "PHONE";
        }
        if (upper.startsWith("V")) {
            return "VEHICLE";
        }
        if (upper.startsWith("ACC")) {
            return "ACCOUNT";
        }
        if (upper.startsWith("LOC")) {
            return "LOCATION";
        }
        return "PERSON";
    }
}
