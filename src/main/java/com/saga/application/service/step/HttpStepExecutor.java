package com.saga.application.service.step;

import com.saga.domain.model.SagaStep;
import com.saga.domain.model.StepType;
import com.saga.application.service.StepExecutionResult;
import com.saga.domain.exception.StepExecutionException;
import com.saga.infrastructure.service.CircuitBreakerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP step executor implementing the Strategy pattern with circuit breaker protection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpStepExecutor implements StepExecutor {

    private final RestTemplate restTemplate;
    private final CircuitBreakerService circuitBreakerService;

    @Override
    public StepExecutionResult execute(SagaStep step, Map<String, Object> sagaInputData) {
        var config = step.getConfig();
        String url = config.getUrl();
        String method = config.getHttpMethod();
        
        log.info("Executing HTTP {} call to: {} for step: {}", method, url, step.getName());

        try {
            // Extract service name from URL for circuit breaker
            String serviceName = extractServiceName(url);
            
            return circuitBreakerService.execute(serviceName, () -> {
                // TODO: Implement proper HTTP call with request body template processing
                // For now, return a mock successful response
                Map<String, Object> responseData = Map.of(
                    "status", "success",
                    "url", url,
                    "method", method,
                    "timestamp", System.currentTimeMillis()
                );

                return StepExecutionResult.builder()
                        .success(true)
                        .outputData(responseData)
                        .build();
            });
            
        } catch (CircuitBreakerService.CircuitBreakerOpenException e) {
            log.warn("Circuit breaker is open for HTTP step: {}", step.getName());
            throw new StepExecutionException(
                "Service temporarily unavailable: " + e.getMessage(), 
                step.getStepId(), 
                step.getStepId(), 
                e
            );
        } catch (Exception e) {
            log.error("HTTP call failed for step: {}", step.getName(), e);
            throw new StepExecutionException(
                "HTTP call failed: " + e.getMessage(), 
                step.getStepId(), 
                step.getStepId(), 
                e
            );
        }
    }

    @Override
    public StepType getSupportedStepType() {
        return StepType.HTTP_CALL;
    }

    /**
     * Extract service name from URL for circuit breaker
     */
    private String extractServiceName(String url) {
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            return parsedUrl.getHost();
        } catch (Exception e) {
            return "unknown-service";
        }
    }
} 