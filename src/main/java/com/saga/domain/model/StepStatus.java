package com.saga.domain.model;

/**
 * Represents the possible states of a Saga Step.
 */
public enum StepStatus {
    
    /**
     * Step has been created but not yet started
     */
    CREATED,
    
    /**
     * Step is currently being executed
     */
    RUNNING,
    
    /**
     * Step completed successfully
     */
    COMPLETED,
    
    /**
     * Step failed during execution
     */
    FAILED,
    
    /**
     * Step is being compensated (rolled back)
     */
    COMPENSATING,
    
    /**
     * Step has been compensated (rolled back)
     */
    COMPENSATED,
    
    /**
     * Step has timed out
     */
    TIMEOUT,
    
    /**
     * Step is being retried
     */
    RETRYING,
    
    /**
     * Step has been skipped
     */
    SKIPPED
} 