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
 * Business logic step executor implementing the Strategy pattern.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessLogicStepExecutor implements StepExecutor {

    @Override
    public StepExecutionResult execute(SagaStep step, Map<String, Object> sagaInputData) {
        var config = step.getConfig();
        String className = config.getClassName();
        String methodName = config.getMethodName();
        
        log.info("Executing business logic: {}.{} for step: {}", className, methodName, step.getName());

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
            throw new StepExecutionException(
                "Business logic execution failed: " + e.getMessage(), 
                step.getStepId(), 
                step.getStepId(), 
                e
            );
        }
    }

    @Override
    public StepType getSupportedStepType() {
        return StepType.BUSINESS_LOGIC;
    }
} 