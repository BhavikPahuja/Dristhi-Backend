package com.project.tekathon.drishti.service;

import com.project.tekathon.drishti.dto.CaseDtos.CaseListItemResponse;
import com.project.tekathon.drishti.dto.CaseDtos.CaseResponse;
import com.project.tekathon.drishti.dto.CaseDtos.CreateCaseRequest;
import com.project.tekathon.drishti.dto.CaseDtos.UpdateCaseRequest;
import com.project.tekathon.drishti.dto.DocumentDtos.DocumentListItemResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.EvidenceResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.SurveillanceResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonTimelineItemResponse;
import com.project.tekathon.drishti.entity.CaseEntity;
import com.project.tekathon.drishti.entity.DocumentEntity;
import com.project.tekathon.drishti.entity.EvidenceEntity;
import com.project.tekathon.drishti.entity.InvestigationEventEntity;
import com.project.tekathon.drishti.entity.SurveillanceReportEntity;
import com.project.tekathon.drishti.exception.ResourceNotFoundException;
import com.project.tekathon.drishti.repository.CaseRepository;
import com.project.tekathon.drishti.repository.DocumentRepository;
import com.project.tekathon.drishti.repository.EvidenceRepository;
import com.project.tekathon.drishti.repository.EventRepository;
import com.project.tekathon.drishti.repository.SurveillanceRepository;
import com.project.tekathon.drishti.util.IdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;
    private final SurveillanceRepository surveillanceRepository;
    private final EvidenceRepository evidenceRepository;
    private final EventRepository eventRepository;
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
        return toResponse(saved);
    }

    public List<CaseListItemResponse> list() {
        return caseRepository.findAll().stream()
                .sorted(Comparator.comparing(CaseEntity::getCreatedAt).reversed())
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
                .sorted(Comparator.comparing(PersonTimelineItemResponse::timestamp))
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
}
