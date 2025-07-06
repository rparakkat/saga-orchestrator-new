package com.saga.application.service.execution;

import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStep;
import com.saga.domain.model.StepStatus;
import com.saga.domain.exception.SagaExecutionException;
import com.saga.application.service.StepExecutionResult;
import com.saga.application.service.step.StepExecutorFactory;
import com.saga.domain.event.SagaEvent;
import com.saga.application.service.SagaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Template method pattern for saga execution with hooks for customization.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaExecutionTemplate {

    private final StepExecutorFactory stepExecutorFactory;
    private final SagaEventPublisher eventPublisher;

    /**
     * Execute saga using template method pattern
     * 
     * @param saga the saga to execute
     * @return the executed saga
     */
    public Saga execute(Saga saga) {
        try {
            // Pre-execution hook
            beforeExecution(saga);
            
            // Execute saga
            Saga result = doExecute(saga);
            
            // Post-execution hook
            afterExecution(result);
            
            return result;
            
        } catch (Exception e) {
            // Error handling hook
            handleExecutionError(saga, e);
            throw e;
        }
    }

    /**
     * Template method - defines the execution algorithm
     */
    private Saga doExecute(Saga saga) {
        log.info("Starting execution of saga: {}", saga.getSagaId());
        
        // Mark saga as running
        saga.setStatus(com.saga.domain.model.SagaStatus.RUNNING);
        saga.setStartedAt(LocalDateTime.now());
        
        // Publish started event
        eventPublisher.publishSagaStarted(saga);

        try {
            while (!saga.isCompleted() && saga.hasMoreSteps()) {
                SagaStep currentStep = saga.getCurrentStep();
                if (currentStep == null) {
                    break;
                }

                log.info("Executing step {} of saga {}", currentStep.getName(), saga.getSagaId());
                
                // Pre-step hook
                beforeStepExecution(saga, currentStep);
                
                // Execute the step
                StepExecutionResult result = executeStep(currentStep, saga.getInputData());
                
                // Post-step hook
                afterStepExecution(saga, currentStep, result);
                
                if (result.isSuccess()) {
                    // Step succeeded
                    handleStepSuccess(saga, currentStep, result);
                } else {
                    // Step failed
                    handleStepFailure(saga, currentStep, result);
                    break;
                }
            }

            // Check if saga completed successfully
            if (!saga.isCompleted() && !saga.hasMoreSteps()) {
                handleSagaCompletion(saga);
            }

        } catch (Exception e) {
            handleSagaError(saga, e);
        }

        return saga;
    }

    /**
     * Execute a single step
     */
    private StepExecutionResult executeStep(SagaStep step, Map<String, Object> sagaInputData) {
        var executor = stepExecutorFactory.getExecutor(step);
        return executor.execute(step, sagaInputData);
    }

    /**
     * Handle step success
     */
    private void handleStepSuccess(Saga saga, SagaStep step, StepExecutionResult result) {
        step.setStatus(StepStatus.COMPLETED);
        step.setOutputData(result.getOutputData());
        step.setCompletedAt(LocalDateTime.now());
        step.calculateDuration();
        
        // Update saga output data
        saga.getOutputData().putAll(result.getOutputData());
        
        log.info("Step {} completed successfully for saga {}", step.getName(), saga.getSagaId());
        
        // Move to next step
        saga.moveToNextStep();
    }

    /**
     * Handle step failure
     */
    private void handleStepFailure(Saga saga, SagaStep step, StepExecutionResult result) {
        step.setStatus(StepStatus.FAILED);
        step.setErrorMessage(result.getErrorMessage());
        step.setErrorStackTrace(result.getErrorStackTrace());
        step.setCompletedAt(LocalDateTime.now());
        step.calculateDuration();
        
        log.error("Step {} failed for saga {}: {}", step.getName(), saga.getSagaId(), result.getErrorMessage());
        
        // Handle step failure based on configuration
        if (step.canRetry()) {
            saga.setStatus(com.saga.domain.model.SagaStatus.RETRYING);
            saga.incrementRetryCount();
            step.incrementRetryCount();
            log.info("Retrying step {} for saga {}, attempt {}", 
                    step.getName(), saga.getSagaId(), step.getRetryCount());
        } else if (step.isRequired()) {
            saga.setStatus(com.saga.domain.model.SagaStatus.FAILED);
            saga.setErrorMessage("Required step failed: " + step.getName());
        } else {
            log.warn("Non-required step {} failed for saga {}, continuing with next step", 
                    step.getName(), saga.getSagaId());
            saga.moveToNextStep();
        }
    }

    /**
     * Handle saga completion
     */
    private void handleSagaCompletion(Saga saga) {
        saga.setStatus(com.saga.domain.model.SagaStatus.COMPLETED);
        saga.setCompletedAt(LocalDateTime.now());
        log.info("Saga {} completed successfully", saga.getSagaId());
        eventPublisher.publishSagaCompleted(saga);
    }

    /**
     * Handle saga error
     */
    private void handleSagaError(Saga saga, Exception e) {
        log.error("Error executing saga: {}", saga.getSagaId(), e);
        saga.setStatus(com.saga.domain.model.SagaStatus.FAILED);
        saga.setErrorMessage(e.getMessage());
        saga.setCompletedAt(LocalDateTime.now());
        eventPublisher.publishSagaFailed(saga);
    }

    // Template method hooks - can be overridden by subclasses

    /**
     * Hook called before saga execution
     */
    protected void beforeExecution(Saga saga) {
        log.debug("Before execution hook for saga: {}", saga.getSagaId());
    }

    /**
     * Hook called after saga execution
     */
    protected void afterExecution(Saga saga) {
        log.debug("After execution hook for saga: {}", saga.getSagaId());
    }

    /**
     * Hook called before step execution
     */
    protected void beforeStepExecution(Saga saga, SagaStep step) {
        log.debug("Before step execution hook for step: {} in saga: {}", step.getName(), saga.getSagaId());
    }

    /**
     * Hook called after step execution
     */
    protected void afterStepExecution(Saga saga, SagaStep step, StepExecutionResult result) {
        log.debug("After step execution hook for step: {} in saga: {}", step.getName(), saga.getSagaId());
    }

    /**
     * Hook called when execution error occurs
     */
    protected void handleExecutionError(Saga saga, Exception e) {
        log.error("Execution error hook for saga: {}", saga.getSagaId(), e);
    }
} 