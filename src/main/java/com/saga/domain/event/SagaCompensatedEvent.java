package com.saga.domain.event;

import com.saga.domain.model.Saga;

/**
 * Event fired when a saga is compensated (rolled back).
 */
public class SagaCompensatedEvent extends SagaEvent {
    
    public SagaCompensatedEvent(Saga saga) {
        super(saga.getSagaId(), "SAGA_COMPENSATED", saga);
    }
} 