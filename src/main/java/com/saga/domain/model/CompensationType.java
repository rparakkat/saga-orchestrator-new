package com.saga.domain.model;

/**
 * Represents the different types of compensation actions.
 */
public enum CompensationType {
    
    /**
     * HTTP REST API call to undo the action
     */
    HTTP_CALL,
    
    /**
     * Database operation to undo the action
     */
    DATABASE_OPERATION,
    
    /**
     * Message queue operation to undo the action
     */
    MESSAGE_QUEUE,
    
    /**
     * File system operation to undo the action
     */
    FILE_OPERATION,
    
    /**
     * Custom business logic to undo the action
     */
    BUSINESS_LOGIC,
    
    /**
     * No compensation needed
     */
    NONE
} 