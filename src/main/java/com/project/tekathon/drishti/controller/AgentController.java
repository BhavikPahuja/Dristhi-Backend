package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.IntegrationDtos.AgentInvestigateRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.AgentInvestigateResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.AgentSessionResponse;
import com.project.tekathon.drishti.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/agent")
@RequiredArgsConstructor
public class AgentController {

    private final IntegrationService integrationService;

    @PostMapping("/investigate")
    public AgentInvestigateResponse investigate(@RequestBody AgentInvestigateRequest request) {
        return integrationService.investigate(request);
    }

    @GetMapping("/session/{sessionId}")
    public AgentSessionResponse session(@PathVariable String sessionId) {
        return integrationService.session(sessionId);
    }
}
