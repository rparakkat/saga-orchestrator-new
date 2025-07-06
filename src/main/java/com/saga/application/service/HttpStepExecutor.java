package com.saga.application.service;

import com.saga.domain.model.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Executes HTTP-based saga steps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpStepExecutor {

    private final RestTemplate restTemplate;

    /**
     * Execute an HTTP step
     */
    public StepExecutionResult execute(SagaStep step, Map<String, Object> sagaInputData) {
        var config = step.getConfig();
        String url = config.getUrl();
        String method = config.getHttpMethod();
        
        log.info("Executing HTTP {} call to: {}", method, url);

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
            return StepExecutionResult.builder()
                    .success(false)
                    .errorMessage("HTTP call failed: " + e.getMessage())
                    .errorStackTrace(getStackTrace(e))
                    .build();
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
} 