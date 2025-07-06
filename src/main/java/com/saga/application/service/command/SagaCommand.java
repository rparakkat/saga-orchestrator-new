package com.saga.application.service.command;

import com.saga.domain.model.Saga;

/**
 * Command pattern interface for saga operations.
 */
public interface SagaCommand {
    
    /**
     * Execute the command
     * 
     * @return the result saga
     */
    Saga execute();
    
    /**
     * Undo the command (if possible)
     * 
     * @return the result saga
     */
    Saga undo();
    
    /**
     * Get command description
     * 
     * @return command description
     */
    String getDescription();
} 