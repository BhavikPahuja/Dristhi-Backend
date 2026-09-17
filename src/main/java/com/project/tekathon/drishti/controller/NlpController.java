package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.IntegrationDtos.EntityResolutionRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.EntityResolutionResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.NlpExtractRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.NlpExtractionResponse;
import com.project.tekathon.drishti.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/nlp")
@RequiredArgsConstructor
public class NlpController {

    private final IntegrationService integrationService;

    @PostMapping("/extract")
    public NlpExtractionResponse extract(@RequestBody NlpExtractRequest request) {
        return integrationService.extract(request);
    }

    @PostMapping("/entity-resolution")
    public EntityResolutionResponse entityResolution(@RequestBody EntityResolutionRequest request) {
        return integrationService.resolve(request);
    }
}
