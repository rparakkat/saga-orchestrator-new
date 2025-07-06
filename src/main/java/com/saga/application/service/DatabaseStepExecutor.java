package com.saga.application.service;

import com.saga.domain.model.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Executes database-based saga steps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseStepExecutor {

    /**
     * Execute a database step
     */
    public StepExecutionResult execute(SagaStep step, Map<String, Object> sagaInputData) {
        var config = step.getConfig();
        String query = config.getQuery();
        
        log.info("Executing database operation: {}", query);

        try {
            // TODO: Implement proper database operation execution
            // For now, return a mock successful response
            Map<String, Object> responseData = Map.of(
                "status", "success",
                "query", query,
                "rowsAffected", 1,
                "timestamp", System.currentTimeMillis()
            );

            return StepExecutionResult.builder()
                    .success(true)
                    .outputData(responseData)
                    .build();

        } catch (Exception e) {
            log.error("Database operation failed for step: {}", step.getName(), e);
            return StepExecutionResult.builder()
                    .success(false)
                    .errorMessage("Database operation failed: " + e.getMessage())
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