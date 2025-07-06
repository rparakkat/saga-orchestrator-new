package com.saga.domain.event;

import com.saga.domain.model.Saga;

/**
 * Event fired when a saga completes successfully.
 */
public class SagaCompletedEvent extends SagaEvent {
    
    public SagaCompletedEvent(Saga saga) {
        super(saga.getSagaId(), "SAGA_COMPLETED", saga);
    }
} 