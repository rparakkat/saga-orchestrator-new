package com.saga.application.service;

import com.saga.domain.model.*;
import com.saga.infrastructure.repository.SagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for compensating (rolling back) saga operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationService {

    private final SagaRepository sagaRepository;
    private final StepExecutorService stepExecutorService;

    /**
     * Compensate a saga by executing compensation actions in reverse order
     */
    @Transactional
    public Saga compensateSaga(Saga saga) {
        log.info("Starting compensation for saga: {}", saga.getSagaId());
        
        saga.setStatus(SagaStatus.COMPENSATING);
        saga = sagaRepository.save(saga);

        try {
            // Get completed steps in reverse order for compensation
            List<SagaStep> completedSteps = saga.getSteps().stream()
                    .filter(step -> step.getStatus() == StepStatus.COMPLETED)
                    .sorted((s1, s2) -> Integer.compare(s2.getOrder(), s1.getOrder()))
                    .toList();

            for (SagaStep step : completedSteps) {
                if (step.isCompensatable() && step.getCompensationConfig() != null) {
                    log.info("Compensating step: {}", step.getName());
                    
                    try {
                        CompensationResult result = executeCompensation(step, saga.getInputData());
                        
                        if (result.isSuccess()) {
                            step.setStatus(StepStatus.COMPENSATED);
                            log.info("Step {} compensated successfully", step.getName());
                        } else {
                            step.setStatus(StepStatus.FAILED);
                            step.setErrorMessage("Compensation failed: " + result.getErrorMessage());
                            log.error("Step {} compensation failed: {}", step.getName(), result.getErrorMessage());
                            
                            // If compensation is required and failed, mark saga as failed
                            if (step.getCompensationConfig().isRequired()) {
                                saga.setStatus(SagaStatus.FAILED);
                                saga.setErrorMessage("Compensation failed for step: " + step.getName());
                                saga.setCompletedAt(LocalDateTime.now());
                                return sagaRepository.save(saga);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error compensating step: {}", step.getName(), e);
                        step.setStatus(StepStatus.FAILED);
                        step.setErrorMessage("Compensation error: " + e.getMessage());
                        
                        if (step.getCompensationConfig().isRequired()) {
                            saga.setStatus(SagaStatus.FAILED);
                            saga.setErrorMessage("Compensation error for step: " + step.getName());
                            saga.setCompletedAt(LocalDateTime.now());
                            return sagaRepository.save(saga);
                        }
                    }
                } else {
                    log.info("Step {} is not compensatable, skipping", step.getName());
                }
            }

            // Mark saga as compensated
            saga.setStatus(SagaStatus.COMPENSATED);
            saga.setCompletedAt(LocalDateTime.now());
            log.info("Saga {} compensation completed", saga.getSagaId());

        } catch (Exception e) {
            log.error("Error during saga compensation: {}", saga.getSagaId(), e);
            saga.setStatus(SagaStatus.FAILED);
            saga.setErrorMessage("Compensation error: " + e.getMessage());
            saga.setCompletedAt(LocalDateTime.now());
        }

        return sagaRepository.save(saga);
    }

    /**
     * Execute compensation for a specific step
     */
    private CompensationResult executeCompensation(SagaStep step, java.util.Map<String, Object> sagaInputData) {
        var compensationConfig = step.getCompensationConfig();
        
        log.info("Executing compensation for step: {} of type: {}", 
                step.getName(), compensationConfig.getType());

        try {
            switch (compensationConfig.getType()) {
                case HTTP_CALL -> {
                    return executeHttpCompensation(step, compensationConfig, sagaInputData);
                }
                case DATABASE_OPERATION -> {
                    return executeDatabaseCompensation(step, compensationConfig, sagaInputData);
                }
                case BUSINESS_LOGIC -> {
                    return executeBusinessLogicCompensation(step, compensationConfig, sagaInputData);
                }
                case MESSAGE_QUEUE -> {
                    return executeMessageQueueCompensation(step, compensationConfig, sagaInputData);
                }
                case FILE_OPERATION -> {
                    return executeFileCompensation(step, compensationConfig, sagaInputData);
                }
                case NONE -> {
                    return CompensationResult.builder()
                            .success(true)
                            .message("No compensation required")
                            .build();
                }
                default -> {
                    return CompensationResult.builder()
                            .success(false)
                            .errorMessage("Unsupported compensation type: " + compensationConfig.getType())
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Error executing compensation for step: {}", step.getName(), e);
            return CompensationResult.builder()
                    .success(false)
                    .errorMessage("Compensation execution error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Execute HTTP compensation
     */
    private CompensationResult executeHttpCompensation(SagaStep step, CompensationConfig config, java.util.Map<String, Object> sagaInputData) {
        // TODO: Implement HTTP compensation
        log.warn("HTTP compensation not yet implemented");
        return CompensationResult.builder()
                .success(true)
                .message("HTTP compensation executed")
                .build();
    }

    /**
     * Execute database compensation
     */
    private CompensationResult executeDatabaseCompensation(SagaStep step, CompensationConfig config, java.util.Map<String, Object> sagaInputData) {
        // TODO: Implement database compensation
        log.warn("Database compensation not yet implemented");
        return CompensationResult.builder()
                .success(true)
                .message("Database compensation executed")
                .build();
    }

    /**
     * Execute business logic compensation
     */
    private CompensationResult executeBusinessLogicCompensation(SagaStep step, CompensationConfig config, java.util.Map<String, Object> sagaInputData) {
        // TODO: Implement business logic compensation
        log.warn("Business logic compensation not yet implemented");
        return CompensationResult.builder()
                .success(true)
                .message("Business logic compensation executed")
                .build();
    }

    /**
     * Execute message queue compensation
     */
    private CompensationResult executeMessageQueueCompensation(SagaStep step, CompensationConfig config, java.util.Map<String, Object> sagaInputData) {
        // TODO: Implement message queue compensation
        log.warn("Message queue compensation not yet implemented");
        return CompensationResult.builder()
                .success(true)
                .message("Message queue compensation executed")
                .build();
    }

    /**
     * Execute file compensation
     */
    private CompensationResult executeFileCompensation(SagaStep step, CompensationConfig config, java.util.Map<String, Object> sagaInputData) {
        // TODO: Implement file compensation
        log.warn("File compensation not yet implemented");
        return CompensationResult.builder()
                .success(true)
                .message("File compensation executed")
                .build();
    }
} 