package com.saga.domain.model;

/**
 * Represents the possible states of a Saga workflow.
 */
public enum SagaStatus {
    
    /**
     * Saga has been created but not yet started
     */
    CREATED,
    
    /**
     * Saga is currently being executed
     */
    RUNNING,
    
    /**
     * Saga has been paused (can be resumed)
     */
    PAUSED,
    
    /**
     * Saga completed successfully
     */
    COMPLETED,
    
    /**
     * Saga failed during execution
     */
    FAILED,
    
    /**
     * Saga is being compensated (rolled back)
     */
    COMPENSATING,
    
    /**
     * Saga has been compensated (rolled back)
     */
    COMPENSATED,
    
    /**
     * Saga has timed out
     */
    TIMEOUT,
    
    /**
     * Saga is being retried
     */
    RETRYING
} 