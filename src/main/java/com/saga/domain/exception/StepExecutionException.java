package com.saga.domain.exception;

/**
 * Exception thrown when step execution fails.
 */
public class StepExecutionException extends SagaException {
    
    private final String stepId;
    
    public StepExecutionException(String message, String sagaId, String stepId) {
        super(message, sagaId, "STEP_EXECUTION_FAILED", ErrorSeverity.MEDIUM);
        this.stepId = stepId;
    }
    
    public StepExecutionException(String message, String sagaId, String stepId, Throwable cause) {
        super(message, sagaId, "STEP_EXECUTION_FAILED", ErrorSeverity.MEDIUM, cause);
        this.stepId = stepId;
    }
    
    public String getStepId() {
        return stepId;
    }
} 