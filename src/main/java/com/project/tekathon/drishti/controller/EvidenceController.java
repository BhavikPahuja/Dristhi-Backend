package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.InvestigationDtos.EvidenceResponse;
import com.project.tekathon.drishti.service.InvestigationDataService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EvidenceController {

    private final InvestigationDataService investigationDataService;

    @GetMapping("/evidence/{evidenceId}")
    public EvidenceResponse get(@PathVariable String evidenceId) {
        return investigationDataService.getEvidence(evidenceId);
    }

    @GetMapping("/cases/{caseId}/evidence")
    public List<EvidenceResponse> caseEvidence(@PathVariable String caseId) {
        return investigationDataService.caseEvidence(caseId);
    }

    @GetMapping("/entities/{entityId}/evidence")
    public List<EvidenceResponse> entityEvidence(@PathVariable String entityId) {
        return investigationDataService.entityEvidence(entityId);
    }

    @GetMapping("/relationships/{relationshipId}/evidence")
    public List<EvidenceResponse> relationshipEvidence(@PathVariable String relationshipId) {
        return investigationDataService.relationshipEvidence(relationshipId);
    }
}
