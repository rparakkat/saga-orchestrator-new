package com.saga.application.service.step;

import com.saga.domain.model.SagaStep;
import com.saga.application.service.StepExecutionResult;

import java.util.Map;

/**
 * Strategy interface for step executors.
 * Each step type implements this interface to provide specific execution logic.
 */
public interface StepExecutor {
    
    /**
     * Execute a step
     * 
     * @param step the step to execute
     * @param sagaInputData input data from the saga
     * @return execution result
     */
    StepExecutionResult execute(SagaStep step, Map<String, Object> sagaInputData);
    
    /**
     * Get the step type this executor handles
     * 
     * @return step type
     */
    com.saga.domain.model.StepType getSupportedStepType();
    
    /**
     * Check if this executor can handle the given step
     * 
     * @param step the step to check
     * @return true if supported
     */
    default boolean canExecute(SagaStep step) {
        return step.getType() == getSupportedStepType();
    }
} 