package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.IntegrationDtos.MlAnalyzeRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlAnalyzeResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlAnomalyResult;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlCommunityResult;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlKeyActorResult;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlPotentialLinkResult;
import com.project.tekathon.drishti.service.IntegrationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/ml")
@RequiredArgsConstructor
public class MlController {

    private final IntegrationService integrationService;

    @PostMapping("/analyze")
    public MlAnalyzeResponse analyze(@RequestBody MlAnalyzeRequest request) {
        return integrationService.analyze(request);
    }

    @GetMapping("/anomalies")
    public List<MlAnomalyResult> anomalies() {
        return integrationService.anomalies();
    }

    @GetMapping("/person/{personId}/anomalies")
    public List<MlAnomalyResult> personAnomalies(@PathVariable String personId) {
        return integrationService.personAnomalies(personId);
    }

    @GetMapping("/key-actors")
    public List<MlKeyActorResult> keyActors() {
        return integrationService.keyActors();
    }

    @GetMapping("/communities")
    public List<MlCommunityResult> communities() {
        return integrationService.communities();
    }

    @GetMapping("/potential-links")
    public List<MlPotentialLinkResult> potentialLinks() {
        return integrationService.potentialLinks();
    }
}
