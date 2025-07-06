package com.saga.domain.event;

import com.saga.domain.model.Saga;

/**
 * Event fired when a saga starts execution.
 */
public class SagaStartedEvent extends SagaEvent {
    
    public SagaStartedEvent(Saga saga) {
        super(saga.getSagaId(), "SAGA_STARTED", saga);
    }
} 