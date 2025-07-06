package com.saga.domain.model;

/**
 * Represents the different types of steps that can be executed in a Saga.
 */
public enum StepType {
    
    /**
     * HTTP REST API call
     */
    HTTP_CALL,
    
    /**
     * Database operation
     */
    DATABASE_OPERATION,
    
    /**
     * Message queue operation
     */
    MESSAGE_QUEUE,
    
    /**
     * File system operation
     */
    FILE_OPERATION,
    
    /**
     * Custom business logic
     */
    BUSINESS_LOGIC,
    
    /**
     * Wait/delay operation
     */
    WAIT,
    
    /**
     * Conditional step (if/else)
     */
    CONDITIONAL,
    
    /**
     * Parallel execution step
     */
    PARALLEL,
    
    /**
     * Sub-saga execution
     */
    SUB_SAGA
} 