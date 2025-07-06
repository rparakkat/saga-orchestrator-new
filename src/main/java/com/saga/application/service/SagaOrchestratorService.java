package com.saga.application.service;

import com.saga.domain.model.*;
import com.saga.domain.exception.SagaExecutionException;
import com.saga.infrastructure.repository.SagaRepository;
import com.saga.application.service.execution.SagaExecutionTemplate;
import com.saga.application.service.command.SagaCommand;
import com.saga.application.service.command.ExecuteSagaCommand;
import com.saga.application.service.builder.SagaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Core service for orchestrating Saga workflows.
 * Handles saga execution, compensation, and error recovery.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestratorService {

    private final SagaRepository sagaRepository;
    private final SagaExecutionTemplate executionTemplate;
    private final CompensationService compensationService;
    private final SagaEventPublisher eventPublisher;

    /**
     * Create and start a new saga
     */
    @Transactional
    public Saga createAndStartSaga(String name, List<SagaStep> steps, Map<String, Object> inputData) {
        String sagaId = generateSagaId();
        
        Saga saga = Saga.builder()
                .sagaId(sagaId)
                .name(name)
                .status(SagaStatus.CREATED)
                .steps(steps)
                .currentStepIndex(0)
                .inputData(inputData)
                .outputData(Map.of())
                .retryCount(0)
                .maxRetries(3)
                .timeoutMs(30000L)
                .priority(0)
                .build();

        saga = sagaRepository.save(saga);
        log.info("Created saga: {}", sagaId);

        // Start execution asynchronously
        executeSagaAsync(sagaId);
        
        return saga;
    }

    /**
     * Execute a saga asynchronously
     */
    @Async
    public CompletableFuture<Saga> executeSagaAsync(String sagaId) {
        try {
            return CompletableFuture.completedFuture(executeSaga(sagaId));
        } catch (Exception e) {
            log.error("Error executing saga: {}", sagaId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Execute a saga synchronously
     */
    @Transactional
    public Saga executeSaga(String sagaId) {
        Saga saga = sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new SagaExecutionException("Saga not found: " + sagaId, sagaId));

        if (saga.isCompleted()) {
            log.warn("Saga {} is already completed with status: {}", sagaId, saga.getStatus());
            return saga;
        }

        try {
            // Use Command pattern for execution
            SagaCommand command = new ExecuteSagaCommand(executionTemplate, saga);
            saga = command.execute();
            
            return sagaRepository.save(saga);
            
        } catch (Exception e) {
            log.error("Error executing saga: {}", sagaId, e);
            throw new SagaExecutionException("Failed to execute saga: " + e.getMessage(), sagaId, e);
        }
    }



    /**
     * Retry a failed saga
     */
    @Transactional
    public Saga retrySaga(String sagaId) {
        Saga saga = sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + sagaId));

        if (!saga.canRetry()) {
            throw new IllegalStateException("Saga cannot be retried: " + sagaId);
        }

        log.info("Retrying saga: {}", sagaId);
        saga.setStatus(SagaStatus.RUNNING);
        saga.setRetryCount(0);
        saga.setErrorMessage(null);
        saga.setErrorStackTrace(null);
        saga.setCompletedAt(null);
        
        saga = sagaRepository.save(saga);
        
        // Execute asynchronously
        executeSagaAsync(sagaId);
        
        return saga;
    }

    /**
     * Compensate a saga (rollback)
     */
    @Transactional
    public Saga compensateSaga(String sagaId) {
        Saga saga = sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + sagaId));

        if (saga.getStatus() != SagaStatus.FAILED && saga.getStatus() != SagaStatus.RUNNING) {
            throw new IllegalStateException("Saga cannot be compensated: " + sagaId);
        }

        log.info("Compensating saga: {}", sagaId);
        return compensationService.compensateSaga(saga);
    }

    /**
     * Get saga by ID
     */
    public Saga getSaga(String sagaId) {
        return sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + sagaId));
    }

    /**
     * Get sagas by status
     */
    public List<Saga> getSagasByStatus(SagaStatus status) {
        return sagaRepository.findByStatus(status);
    }

    /**
     * Get sagas by correlation ID
     */
    public List<Saga> getSagasByCorrelationId(String correlationId) {
        return sagaRepository.findByCorrelationId(correlationId);
    }

    /**
     * Generate unique saga ID
     */
    private String generateSagaId() {
        return "saga-" + UUID.randomUUID().toString();
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
} 