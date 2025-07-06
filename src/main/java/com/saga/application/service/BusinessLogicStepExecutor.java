package com.saga.application.service;

import com.saga.domain.model.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Executes business logic-based saga steps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessLogicStepExecutor {

    /**
     * Execute a business logic step
     */
    public StepExecutionResult execute(SagaStep step, Map<String, Object> sagaInputData) {
        var config = step.getConfig();
        String className = config.getClassName();
        String methodName = config.getMethodName();
        
        log.info("Executing business logic: {}.{}", className, methodName);

        try {
            // TODO: Implement proper business logic execution using reflection
            // For now, return a mock successful response
            Map<String, Object> responseData = Map.of(
                "status", "success",
                "className", className,
                "methodName", methodName,
                "timestamp", System.currentTimeMillis()
            );

            return StepExecutionResult.builder()
                    .success(true)
                    .outputData(responseData)
                    .build();

        } catch (Exception e) {
            log.error("Business logic execution failed for step: {}", step.getName(), e);
            return StepExecutionResult.builder()
                    .success(false)
                    .errorMessage("Business logic execution failed: " + e.getMessage())
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