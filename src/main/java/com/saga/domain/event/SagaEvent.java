package com.saga.domain.event;

import com.saga.domain.model.Saga;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Base event class for saga events using the Observer pattern.
 */
@Getter
public abstract class SagaEvent {
    
    private final String sagaId;
    private final String eventType;
    private final LocalDateTime timestamp;
    private final Saga saga;
    
    protected SagaEvent(String sagaId, String eventType, Saga saga) {
        this.sagaId = sagaId;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.saga = saga;
    }
} 