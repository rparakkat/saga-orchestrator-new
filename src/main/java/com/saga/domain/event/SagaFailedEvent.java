package com.saga.domain.event;

import com.saga.domain.model.Saga;

/**
 * Event fired when a saga fails.
 */
public class SagaFailedEvent extends SagaEvent {
    
    public SagaFailedEvent(Saga saga) {
        super(saga.getSagaId(), "SAGA_FAILED", saga);
    }
} 