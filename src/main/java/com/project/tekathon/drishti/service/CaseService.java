package com.project.tekathon.drishti.service;

import com.project.tekathon.drishti.dto.CaseDtos.CaseListItemResponse;
import com.project.tekathon.drishti.dto.CaseDtos.CaseResponse;
import com.project.tekathon.drishti.dto.CaseDtos.CreateCaseRequest;
import com.project.tekathon.drishti.dto.CaseDtos.UpdateCaseRequest;
import com.project.tekathon.drishti.dto.DocumentDtos.DocumentListItemResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.EvidenceResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.SurveillanceResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonTimelineItemResponse;
import com.project.tekathon.drishti.entity.AccountEntity;
import com.project.tekathon.drishti.entity.CaseEntity;
import com.project.tekathon.drishti.entity.DocumentEntity;
import com.project.tekathon.drishti.entity.EvidenceEntity;
import com.project.tekathon.drishti.entity.InvestigationEventEntity;
import com.project.tekathon.drishti.entity.PersonEntity;
import com.project.tekathon.drishti.entity.PhoneEntity;
import com.project.tekathon.drishti.entity.RelationshipEntity;
import com.project.tekathon.drishti.entity.SurveillanceReportEntity;
import com.project.tekathon.drishti.entity.VehicleEntity;
import com.project.tekathon.drishti.exception.ResourceNotFoundException;
import com.project.tekathon.drishti.repository.AccountRepository;
import com.project.tekathon.drishti.repository.CaseRepository;
import com.project.tekathon.drishti.repository.DocumentRepository;
import com.project.tekathon.drishti.repository.EvidenceRepository;
import com.project.tekathon.drishti.repository.EventRepository;
import com.project.tekathon.drishti.repository.PersonRepository;
import com.project.tekathon.drishti.repository.PhoneRepository;
import com.project.tekathon.drishti.repository.RelationshipRepository;
import com.project.tekathon.drishti.repository.SurveillanceRepository;
import com.project.tekathon.drishti.repository.VehicleRepository;
import com.project.tekathon.drishti.util.IdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;
    private final SurveillanceRepository surveillanceRepository;
    private final EvidenceRepository evidenceRepository;
    private final EventRepository eventRepository;
    private final PersonRepository personRepository;
    private final RelationshipRepository relationshipRepository;
    private final PhoneRepository phoneRepository;
    private final AccountRepository accountRepository;
    private final VehicleRepository vehicleRepository;
    private final NetworkService networkService;

    public CaseResponse create(CreateCaseRequest request) {
        long next = caseRepository.count() + 1;
        CaseEntity entity = CaseEntity.builder()
                .caseId(IdGenerator.next("CASE", next))
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .build();
        CaseEntity saved = caseRepository.save(entity);
        networkService.syncCase(saved);
        autoIngestForensicEntities(saved);
        return toResponse(saved);
    }

    public List<CaseListItemResponse> list() {
        return caseRepository.findAll().stream()
                .sorted(Comparator.comparing(CaseEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(caseEntity -> new CaseListItemResponse(
                        caseEntity.getCaseId(),
                        caseEntity.getTitle(),
                        caseEntity.getStatus(),
                        caseEntity.getCreatedAt()))
                .toList();
    }

    public CaseResponse get(String caseId) {
        return toResponse(findCase(caseId));
    }

    public CaseResponse update(String caseId, UpdateCaseRequest request) {
        CaseEntity entity = findCase(caseId);
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setStatus(request.status());
        return toResponse(caseRepository.save(entity));
    }

    public void delete(String caseId) {
        CaseEntity entity = findCase(caseId);
        caseRepository.delete(entity);
    }

    public List<DocumentListItemResponse> documents(String caseId) {
        findCase(caseId);
        return documentRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .map(document -> new DocumentListItemResponse(
                        document.getDocumentId(),
                        document.getCaseId(),
                        document.getDocumentType(),
                        document.getTitle(),
                        document.getSource(),
                        document.getCreatedAt()))
                .toList();
    }

    public List<SurveillanceResponse> surveillance(String caseId) {
        findCase(caseId);
        return surveillanceRepository.findByCaseIdOrderByTimestampDesc(caseId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EvidenceResponse> evidence(String caseId) {
        findCase(caseId);
        return evidenceRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PersonResponse> persons(String caseId) {
        findCase(caseId);
        List<String> caseEntityIds = new ArrayList<>();
        relationshipRepository.findAll().stream()
                .filter(rel -> caseId.equalsIgnoreCase(rel.getCaseId()))
                .forEach(rel -> {
                    caseEntityIds.add(rel.getSourceId());
                    caseEntityIds.add(rel.getTargetId());
                });
        evidenceRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .filter(ev -> ev.getEntityId() != null)
                .forEach(ev -> caseEntityIds.add(ev.getEntityId()));

        List<PersonEntity> matchedPersons;
        if (caseEntityIds.isEmpty()) {
            matchedPersons = personRepository.findAll();
        } else {
            matchedPersons = personRepository.findAll().stream()
                    .filter(p -> caseEntityIds.contains(p.getPersonId()))
                    .distinct()
                    .toList();
            if (matchedPersons.isEmpty()) {
                matchedPersons = personRepository.findAll();
            }
        }

        return matchedPersons.stream()
                .map(p -> new PersonResponse(p.getPersonId(), p.getName(), p.getAliases(), p.getAge(), p.getDateOfBirth(), p.getAddress(), p.getStatus(), p.getCreatedAt(), p.getUpdatedAt()))
                .toList();
    }

    public List<DocumentListItemResponse> fir(String caseId) {
        findCase(caseId);
        return documentRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .filter(doc -> doc.getDocumentType() != null && (
                        doc.getDocumentType().equalsIgnoreCase("FIR") ||
                        doc.getDocumentType().equalsIgnoreCase("BAIL_JUDGMENT") ||
                        doc.getDocumentType().equalsIgnoreCase("HANDWRITTEN_NOTE") ||
                        doc.getDocumentType().equalsIgnoreCase("JUDGMENT") ||
                        doc.getDocumentType().equalsIgnoreCase("OTHER")
                ))
                .map(document -> new DocumentListItemResponse(
                        document.getDocumentId(),
                        document.getCaseId(),
                        document.getDocumentType(),
                        document.getTitle(),
                        document.getSource(),
                        document.getCreatedAt()))
                .toList();
    }

    public List<PersonTimelineItemResponse> timeline(String caseId) {
        findCase(caseId);
        List<PersonTimelineItemResponse> timeline = new ArrayList<>();
        for (DocumentEntity document : documentRepository.findByCaseIdOrderByCreatedAtDesc(caseId)) {
            timeline.add(new PersonTimelineItemResponse(
                    document.getDocumentId(),
                    "DOCUMENT",
                    document.getCreatedAt(),
                    document.getTitle(),
                    document.getDocumentId()));
        }
        for (InvestigationEventEntity event : eventRepository.findByCaseIdOrderByTimestampDesc(caseId)) {
            timeline.add(new PersonTimelineItemResponse(
                    event.getEventId(),
                    event.getEventType(),
                    event.getTimestamp(),
                    event.getDescription(),
                    event.getSourceDocumentId()));
        }
        for (SurveillanceReportEntity report : surveillanceRepository.findByCaseIdOrderByTimestampDesc(caseId)) {
            timeline.add(new PersonTimelineItemResponse(
                    report.getReportId(),
                    "SURVEILLANCE",
                    report.getTimestamp(),
                    report.getDescription(),
                    null));
        }
        return timeline.stream()
                .sorted(Comparator.comparing(PersonTimelineItemResponse::timestamp, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public CaseEntity findCase(String caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case " + caseId + " not found"));
    }

    private CaseResponse toResponse(CaseEntity entity) {
        return new CaseResponse(entity.getCaseId(), entity.getTitle(), entity.getDescription(), entity.getStatus(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private SurveillanceResponse toResponse(SurveillanceReportEntity report) {
        return new SurveillanceResponse(report.getReportId(), report.getCaseId(), report.getDescription(),
                report.getTimestamp(), report.getLocationId(), report.getVehicleId(), report.getPersonId(), report.getSource());
    }

    private EvidenceResponse toResponse(EvidenceEntity evidence) {
        return new EvidenceResponse(evidence.getEvidenceId(), evidence.getCaseId(), evidence.getType(),
                evidence.getDescription(), evidence.getSourceDocumentId(), evidence.getEntityId(),
                evidence.getRelationshipId(), evidence.getSourceSpanText());
    }

    private void autoIngestForensicEntities(CaseEntity caseEntity) {
        if (caseEntity.getDescription() == null || caseEntity.getDescription().isBlank()) {
            return;
        }
        String desc = caseEntity.getDescription();

        try {
            // 1. Create DocumentEntity for case description text
            String docId = IdGenerator.next("DOC", documentRepository.count() + 1);
            DocumentEntity doc = DocumentEntity.builder()
                    .documentId(docId)
                    .caseId(caseEntity.getCaseId())
                    .title(caseEntity.getTitle() == null ? "Case Document " + docId : caseEntity.getTitle())
                    .text(desc)
                    .documentType("HANDWRITTEN_NOTE")
                    .source("AI_AGENT_IMPORT")
                    .language("en")
                    .build();
            documentRepository.save(doc);
            try {
                networkService.syncDocument(doc);
            } catch (Exception ignored) {}

            List<String> extractedPersonIds = new ArrayList<>();

            // 2. Extract Persons
            Pattern personPattern = Pattern.compile("(?:Persons?|Sender|Receiver|Met|Target|Suspect|Beneficiary):?\\s*([A-Za-z\\s]{3,30})(?:\\(|,|$|\n)", Pattern.CASE_INSENSITIVE);
            Matcher personMatcher = personPattern.matcher(desc);
            while (personMatcher.find()) {
                String rawName = personMatcher.group(1).trim();
                if (rawName.length() >= 3 && !rawName.equalsIgnoreCase("Location") && !rawName.equalsIgnoreCase("Date") && !rawName.equalsIgnoreCase("Case") && !rawName.equalsIgnoreCase("Amount") && !rawName.equalsIgnoreCase("Amounts")) {
                    String personName = rawName;
                    var existing = personRepository.findAll().stream()
                            .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(personName))
                            .findFirst();
                    String pId;
                    if (existing.isPresent()) {
                        pId = existing.get().getPersonId();
                    } else {
                        pId = IdGenerator.next("P", personRepository.count() + 1);
                        PersonEntity p = PersonEntity.builder()
                                .personId(pId)
                                .name(personName)
                                .status("EXTRACTED_PERSON")
                                .notes("Extracted from case " + caseEntity.getCaseId())
                                .build();
                        PersonEntity savedP = personRepository.save(p);
                        try {
                            networkService.syncPerson(savedP);
                        } catch (Exception ignored) {}
                    }
                    if (!extractedPersonIds.contains(pId)) {
                        extractedPersonIds.add(pId);
                    }
                }
            }

            // 3. Extract Phone Numbers
            Pattern phonePattern = Pattern.compile("(?:Phones?|Emergency Phone|Mobile)?:?\\s*(\\+?\\d{10,12})", Pattern.CASE_INSENSITIVE);
            Matcher phoneMatcher = phonePattern.matcher(desc);
            while (phoneMatcher.find()) {
                String phoneNum = phoneMatcher.group(1).trim();
                if (phoneRepository.findByPhoneNumber(phoneNum).isEmpty()) {
                    String phId = IdGenerator.next("PH", phoneRepository.count() + 1);
                    String owner = extractedPersonIds.isEmpty() ? null : extractedPersonIds.get(0);
                    PhoneEntity phone = PhoneEntity.builder()
                            .phoneId(phId)
                            .phoneNumber(phoneNum)
                            .ownerPersonId(owner == null ? "P-UNKNOWN" : owner)
                            .status("ACTIVE")
                            .build();
                    PhoneEntity savedPh = phoneRepository.save(phone);
                    try {
                        networkService.syncPhone(savedPh);
                    } catch (Exception ignored) {}
                }
            }

            // 4. Extract Bank Accounts (ACC-XXXX)
            Pattern accPattern = Pattern.compile("(?:Accounts?|ACC)?:?\\s*(ACC-\\d{4,12})", Pattern.CASE_INSENSITIVE);
            Matcher accMatcher = accPattern.matcher(desc);
            while (accMatcher.find()) {
                String accNum = accMatcher.group(1).trim();
                if (accountRepository.findByAccountNumber(accNum).isEmpty()) {
                    String accId = IdGenerator.next("ACC", accountRepository.count() + 1);
                    String owner = extractedPersonIds.size() > 1 ? extractedPersonIds.get(1) : (extractedPersonIds.isEmpty() ? "P-UNKNOWN" : extractedPersonIds.get(0));
                    AccountEntity account = AccountEntity.builder()
                            .accountId(accId)
                            .accountNumber(accNum)
                            .accountType("CURRENT")
                            .ownerPersonId(owner)
                            .institution("State Bank")
                            .build();
                    AccountEntity savedAcc = accountRepository.save(account);
                    try {
                        networkService.syncAccount(savedAcc);
                    } catch (Exception ignored) {}
                }
            }

            // 5. Extract Vehicles (e.g. DL-01-AB-1234 or PB-02-AX-4411)
            Pattern vehPattern = Pattern.compile("(?:Vehicles?|Vehicle Sighted)?:?\\s*([A-Z]{2}-\\d{2}-[A-Z]{1,2}-\\d{4})", Pattern.CASE_INSENSITIVE);
            Matcher vehMatcher = vehPattern.matcher(desc);
            while (vehMatcher.find()) {
                String reg = vehMatcher.group(1).trim();
                if (vehicleRepository.findByRegistrationNumber(reg).isEmpty()) {
                    String vehId = IdGenerator.next("VEH", vehicleRepository.count() + 1);
                    String owner = extractedPersonIds.isEmpty() ? null : extractedPersonIds.get(0);
                    VehicleEntity veh = VehicleEntity.builder()
                            .vehicleId(vehId)
                            .registrationNumber(reg)
                            .make("SUV")
                            .model("Forensic Vehicle")
                            .ownerPersonId(owner)
                            .build();
                    VehicleEntity savedVeh = vehicleRepository.save(veh);
                    try {
                        networkService.syncVehicle(savedVeh);
                    } catch (Exception ignored) {}
                }
            }

            // 6. Create Inter-Entity Relationships
            for (int i = 0; i < extractedPersonIds.size() - 1; i++) {
                String p1 = extractedPersonIds.get(i);
                String p2 = extractedPersonIds.get(i + 1);
                String relId = IdGenerator.next("REL", relationshipRepository.count() + 1);
                RelationshipEntity rel = RelationshipEntity.builder()
                        .relationshipId(relId)
                        .caseId(caseEntity.getCaseId())
                        .sourceId(p1)
                        .targetId(p2)
                        .relationship("ASSOCIATED_WITH")
                        .confidence(0.95)
                        .supportText("Extracted from case description: " + caseEntity.getTitle())
                        .build();
                relationshipRepository.save(rel);
                try {
                    networkService.syncRelationship(rel);
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            log.warn("Auto-ingestion of forensic entities failed for case {}: {}", caseEntity.getCaseId(), ex.getMessage());
        }
    }
}
