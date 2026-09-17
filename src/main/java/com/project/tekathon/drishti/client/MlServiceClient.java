package com.project.tekathon.drishti.client;

import com.project.tekathon.drishti.dto.IntegrationDtos.MlAnalyzeRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlAnalyzeResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MlServiceClient extends AbstractPythonServiceClient {

    public MlServiceClient(WebClient.Builder builder, @Value("${ml.service.url}") String baseUrl) {
        super(builder, baseUrl, "ML_SERVICE_UNAVAILABLE", "ML service is currently unavailable");
    }

    public MlAnalyzeResponse analyze(MlAnalyzeRequest request) {
        return post("/analyze", request, MlAnalyzeResponse.class);
    }
}
