package com.saga.application.service.error;

import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStep;
import com.saga.application.service.StepExecutionResult;

/**
 * Chain of Responsibility pattern for error handling.
 */
public abstract class ErrorHandler {
    
    protected ErrorHandler nextHandler;
    
    public void setNext(ErrorHandler handler) {
        this.nextHandler = handler;
    }
    
    /**
     * Handle error in the chain
     * 
     * @param saga the saga
     * @param step the step that failed
     * @param result the execution result
     * @return true if error was handled, false to pass to next handler
     */
    public abstract boolean handleError(Saga saga, SagaStep step, StepExecutionResult result);
    
    /**
     * Pass to next handler in chain
     */
    protected boolean passToNext(Saga saga, SagaStep step, StepExecutionResult result) {
        if (nextHandler != null) {
            return nextHandler.handleError(saga, step, result);
        }
        return false; // No more handlers in chain
    }
} 