package com.saga.domain.exception;

import lombok.Getter;

/**
 * Base exception for all saga-related errors.
 */
@Getter
public abstract class SagaException extends RuntimeException {
    
    private final String sagaId;
    private final String errorCode;
    private final ErrorSeverity severity;
    
    protected SagaException(String message, String sagaId, String errorCode, ErrorSeverity severity) {
        super(message);
        this.sagaId = sagaId;
        this.errorCode = errorCode;
        this.severity = severity;
    }
    
    protected SagaException(String message, String sagaId, String errorCode, ErrorSeverity severity, Throwable cause) {
        super(message, cause);
        this.sagaId = sagaId;
        this.errorCode = errorCode;
        this.severity = severity;
    }
    
    /**
     * Error severity levels
     */
    public enum ErrorSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
} 