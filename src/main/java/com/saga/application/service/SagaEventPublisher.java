package com.saga.application.service;

import com.saga.domain.model.Saga;
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

    /**
     * Publish saga started event
     */
    public void publishSagaStarted(Saga saga) {
        log.info("Publishing saga started event for: {}", saga.getSagaId());
        // TODO: Implement actual event publishing
        // eventPublisher.publishEvent(new SagaStartedEvent(saga));
    }

    /**
     * Publish saga completed event
     */
    public void publishSagaCompleted(Saga saga) {
        log.info("Publishing saga completed event for: {}", saga.getSagaId());
        // TODO: Implement actual event publishing
        // eventPublisher.publishEvent(new SagaCompletedEvent(saga));
    }

    /**
     * Publish saga failed event
     */
    public void publishSagaFailed(Saga saga) {
        log.info("Publishing saga failed event for: {}", saga.getSagaId());
        // TODO: Implement actual event publishing
        // eventPublisher.publishEvent(new SagaFailedEvent(saga));
    }

    /**
     * Publish saga compensated event
     */
    public void publishSagaCompensated(Saga saga) {
        log.info("Publishing saga compensated event for: {}", saga.getSagaId());
        // TODO: Implement actual event publishing
        // eventPublisher.publishEvent(new SagaCompensatedEvent(saga));
    }
} 