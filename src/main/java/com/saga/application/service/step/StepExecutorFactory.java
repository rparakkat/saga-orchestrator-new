package com.saga.application.service.step;

import com.saga.domain.model.SagaStep;
import com.saga.domain.model.StepType;
import com.saga.domain.exception.StepExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating step executors using the Factory pattern.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StepExecutorFactory {

    private final List<StepExecutor> stepExecutors;

    /**
     * Get the appropriate executor for a step
     * 
     * @param step the step to execute
     * @return the executor
     * @throws StepExecutionException if no executor is found
     */
    public StepExecutor getExecutor(SagaStep step) {
        return stepExecutors.stream()
                .filter(executor -> executor.canExecute(step))
                .findFirst()
                .orElseThrow(() -> new StepExecutionException(
                    "No executor found for step type: " + step.getType(),
                    step.getStepId(),
                    step.getStepId()
                ));
    }

    /**
     * Get executor by step type
     * 
     * @param stepType the step type
     * @return the executor
     */
    public Optional<StepExecutor> getExecutorByType(StepType stepType) {
        return stepExecutors.stream()
                .filter(executor -> executor.getSupportedStepType() == stepType)
                .findFirst();
    }

    /**
     * Check if an executor exists for the given step type
     * 
     * @param stepType the step type
     * @return true if executor exists
     */
    public boolean hasExecutor(StepType stepType) {
        return getExecutorByType(stepType).isPresent();
    }
} 