package com.project.tekathon.drishti.client;

import com.project.tekathon.drishti.exception.ServiceUnavailableException;
import java.time.Duration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

abstract class AbstractPythonServiceClient {

    private final WebClient webClient;
    private final String errorCode;
    private final String unavailableMessage;

    protected AbstractPythonServiceClient(WebClient.Builder builder, String baseUrl, String errorCode, String unavailableMessage) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.errorCode = errorCode;
        this.unavailableMessage = unavailableMessage;
    }

    protected <T> T post(String path, Object body, Class<T> responseType) {
        try {
            return webClient.post()
                    .uri(path)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .timeout(Duration.ofSeconds(20))
                    .onErrorMap(throwable -> new ServiceUnavailableException(errorCode, unavailableMessage))
                    .block();
        } catch (ServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceUnavailableException(errorCode, unavailableMessage);
        }
    }

    protected <T> T get(String path, Class<T> responseType) {
        try {
            Mono<T> mono = webClient.get()
                    .uri(path)
                    .retrieve()
                    .bodyToMono(responseType)
                    .timeout(Duration.ofSeconds(20));
            return mono.onErrorMap(throwable -> new ServiceUnavailableException(errorCode, unavailableMessage)).block();
        } catch (ServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceUnavailableException(errorCode, unavailableMessage);
        }
    }
}
