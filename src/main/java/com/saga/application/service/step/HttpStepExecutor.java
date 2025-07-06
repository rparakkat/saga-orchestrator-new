package com.saga.application.service.step;

import com.saga.domain.model.SagaStep;
import com.saga.domain.model.StepType;
import com.saga.application.service.StepExecutionResult;
import com.saga.domain.exception.StepExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP step executor implementing the Strategy pattern.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpStepExecutor implements StepExecutor {

    private final RestTemplate restTemplate;

    @Override
    public StepExecutionResult execute(SagaStep step, Map<String, Object> sagaInputData) {
        var config = step.getConfig();
        String url = config.getUrl();
        String method = config.getHttpMethod();
        
        log.info("Executing HTTP {} call to: {} for step: {}", method, url, step.getName());

        try {
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
} 