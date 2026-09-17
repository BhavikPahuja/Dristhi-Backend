package com.project.tekathon.drishti.client;

import com.project.tekathon.drishti.dto.IntegrationDtos.AgentInvestigateRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.AgentInvestigateResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.AgentSessionResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AgentServiceClient extends AbstractPythonServiceClient {

    public AgentServiceClient(WebClient.Builder builder, @Value("${agent.service.url}") String baseUrl) {
        super(builder, baseUrl, "AGENT_SERVICE_UNAVAILABLE", "Agent service is currently unavailable");
    }

    public AgentInvestigateResponse investigate(AgentInvestigateRequest request) {
        return post("/investigate", request, AgentInvestigateResponse.class);
    }

    public AgentSessionResponse session(String sessionId) {
        return get("/session/" + sessionId, AgentSessionResponse.class);
    }
}
