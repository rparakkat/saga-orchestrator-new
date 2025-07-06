package com.saga.domain.exception;

/**
 * Exception thrown when saga execution fails.
 */
public class SagaExecutionException extends SagaException {
    
    public SagaExecutionException(String message, String sagaId) {
        super(message, sagaId, "SAGA_EXECUTION_FAILED", ErrorSeverity.HIGH);
    }
    
    public SagaExecutionException(String message, String sagaId, Throwable cause) {
        super(message, sagaId, "SAGA_EXECUTION_FAILED", ErrorSeverity.HIGH, cause);
    }
    
    public SagaExecutionException(String message, String sagaId, String errorCode, ErrorSeverity severity) {
        super(message, sagaId, errorCode, severity);
    }
} 