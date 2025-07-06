package com.saga.application.service;

import com.saga.domain.event.SagaEvent;
import com.saga.domain.event.SagaStartedEvent;
import com.saga.domain.event.SagaCompletedEvent;
import com.saga.domain.event.SagaFailedEvent;
import com.saga.domain.event.SagaCompensatedEvent;
import com.saga.domain.model.Saga;
import com.saga.infrastructure.websocket.SagaWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Service for publishing saga events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final SagaWebSocketHandler webSocketHandler;

    /**
     * Publish saga started event
     */
    public void publishSagaStarted(Saga saga) {
        log.info("Publishing saga started event for: {}", saga.getSagaId());
        
        SagaEvent event = new SagaStartedEvent(saga);
        eventPublisher.publishEvent(event);
        
        // Broadcast to WebSocket clients
        webSocketHandler.broadcastSagaEvent(event);
    }

    /**
     * Publish saga completed event
     */
    public void publishSagaCompleted(Saga saga) {
        log.info("Publishing saga completed event for: {}", saga.getSagaId());
        
        SagaEvent event = new SagaCompletedEvent(saga);
        eventPublisher.publishEvent(event);
        
        // Broadcast to WebSocket clients
        webSocketHandler.broadcastSagaEvent(event);
    }

    /**
     * Publish saga failed event
     */
    public void publishSagaFailed(Saga saga) {
        log.info("Publishing saga failed event for: {}", saga.getSagaId());
        
        SagaEvent event = new SagaFailedEvent(saga);
        eventPublisher.publishEvent(event);
        
        // Broadcast to WebSocket clients
        webSocketHandler.broadcastSagaEvent(event);
    }

    /**
     * Publish saga compensated event
     */
    public void publishSagaCompensated(Saga saga) {
        log.info("Publishing saga compensated event for: {}", saga.getSagaId());
        
        SagaEvent event = new SagaCompensatedEvent(saga);
        eventPublisher.publishEvent(event);
        
        // Broadcast to WebSocket clients
        webSocketHandler.broadcastSagaEvent(event);
    }
} 