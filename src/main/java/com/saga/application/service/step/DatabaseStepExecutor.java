package com.saga.application.service.step;

import com.saga.domain.model.SagaStep;
import com.saga.domain.model.StepType;
import com.saga.application.service.StepExecutionResult;
import com.saga.domain.exception.StepExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Database step executor implementing the Strategy pattern.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseStepExecutor implements StepExecutor {

    @Override
    public StepExecutionResult execute(SagaStep step, Map<String, Object> sagaInputData) {
        var config = step.getConfig();
        String query = config.getQuery();
        
        log.info("Executing database operation: {} for step: {}", query, step.getName());

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
            throw new StepExecutionException(
                "Database operation failed: " + e.getMessage(), 
                step.getStepId(), 
                step.getStepId(), 
                e
            );
        }
    }

    @Override
    public StepType getSupportedStepType() {
        return StepType.DATABASE_OPERATION;
    }
} 