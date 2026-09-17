package com.project.tekathon.drishti.client;

import com.project.tekathon.drishti.config.ApplicationProperties;
import com.project.tekathon.drishti.dto.IntegrationDtos.NlpExtractRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.NlpExtractionResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.EntityResolutionRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.EntityResolutionResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class NlpServiceClient extends AbstractPythonServiceClient {

    public NlpServiceClient(WebClient.Builder builder, @Value("${nlp.service.url}") String baseUrl) {
        super(builder, baseUrl, "NLP_SERVICE_UNAVAILABLE", "NLP service is currently unavailable");
    }

    public NlpExtractionResponse extract(NlpExtractRequest request) {
        return post("/extract", request, NlpExtractionResponse.class);
    }

    public EntityResolutionResponse resolve(EntityResolutionRequest request) {
        return post("/entity-resolution", request, EntityResolutionResponse.class);
    }
}
