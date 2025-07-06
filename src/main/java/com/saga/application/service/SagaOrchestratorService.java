package com.saga.application.service;

import com.saga.domain.model.*;
import com.saga.infrastructure.repository.SagaRepository;
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
    private final StepExecutorService stepExecutorService;
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
                .timeoutMs(30000)
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
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + sagaId));

        if (saga.isCompleted()) {
            log.warn("Saga {} is already completed with status: {}", sagaId, saga.getStatus());
            return saga;
        }

        log.info("Starting execution of saga: {}", sagaId);
        saga.setStatus(SagaStatus.RUNNING);
        saga.setStartedAt(LocalDateTime.now());
        saga = sagaRepository.save(saga);

        eventPublisher.publishSagaStarted(saga);

        try {
            while (!saga.isCompleted() && saga.hasMoreSteps()) {
                SagaStep currentStep = saga.getCurrentStep();
                if (currentStep == null) {
                    break;
                }

                log.info("Executing step {} of saga {}", currentStep.getName(), sagaId);
                
                // Execute the step
                StepExecutionResult result = stepExecutorService.executeStep(currentStep, saga.getInputData());
                
                if (result.isSuccess()) {
                    // Step succeeded
                    currentStep.setStatus(StepStatus.COMPLETED);
                    currentStep.setOutputData(result.getOutputData());
                    currentStep.setCompletedAt(LocalDateTime.now());
                    currentStep.calculateDuration();
                    
                    // Update saga output data
                    saga.getOutputData().putAll(result.getOutputData());
                    
                    log.info("Step {} completed successfully for saga {}", currentStep.getName(), sagaId);
                    
                    // Move to next step
                    saga.moveToNextStep();
                    
                } else {
                    // Step failed
                    currentStep.setStatus(StepStatus.FAILED);
                    currentStep.setErrorMessage(result.getErrorMessage());
                    currentStep.setErrorStackTrace(result.getErrorStackTrace());
                    currentStep.setCompletedAt(LocalDateTime.now());
                    currentStep.calculateDuration();
                    
                    log.error("Step {} failed for saga {}: {}", currentStep.getName(), sagaId, result.getErrorMessage());
                    
                    // Handle step failure
                    handleStepFailure(saga, currentStep);
                    break;
                }
                
                saga = sagaRepository.save(saga);
            }

            // Check if saga completed successfully
            if (!saga.isCompleted() && !saga.hasMoreSteps()) {
                saga.setStatus(SagaStatus.COMPLETED);
                saga.setCompletedAt(LocalDateTime.now());
                log.info("Saga {} completed successfully", sagaId);
                eventPublisher.publishSagaCompleted(saga);
            }

        } catch (Exception e) {
            log.error("Error executing saga: {}", sagaId, e);
            saga.setStatus(SagaStatus.FAILED);
            saga.setErrorMessage(e.getMessage());
            saga.setErrorStackTrace(getStackTrace(e));
            saga.setCompletedAt(LocalDateTime.now());
            eventPublisher.publishSagaFailed(saga);
        }

        return sagaRepository.save(saga);
    }

    /**
     * Handle step failure
     */
    private void handleStepFailure(Saga saga, SagaStep failedStep) {
        if (failedStep.canRetry()) {
            // Retry the step
            saga.setStatus(SagaStatus.RETRYING);
            saga.incrementRetryCount();
            failedStep.incrementRetryCount();
            log.info("Retrying step {} for saga {}, attempt {}", 
                    failedStep.getName(), saga.getSagaId(), failedStep.getRetryCount());
            
        } else if (failedStep.isRequired()) {
            // Required step failed and cannot be retried - start compensation
            saga.setStatus(SagaStatus.COMPENSATING);
            log.info("Starting compensation for saga {} due to failed required step {}", 
                    saga.getSagaId(), failedStep.getName());
            
            compensationService.compensateSaga(saga);
            
        } else {
            // Non-required step failed - continue with next step
            log.warn("Non-required step {} failed for saga {}, continuing with next step", 
                    failedStep.getName(), saga.getSagaId());
            saga.moveToNextStep();
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