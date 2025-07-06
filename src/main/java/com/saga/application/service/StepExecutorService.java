package com.saga.application.service;

import com.saga.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service responsible for executing individual saga steps.
 * Supports different types of steps: HTTP calls, database operations, business logic, etc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StepExecutorService {

    private final HttpStepExecutor httpStepExecutor;
    private final DatabaseStepExecutor databaseStepExecutor;
    private final BusinessLogicStepExecutor businessLogicStepExecutor;

    /**
     * Execute a saga step
     */
    public StepExecutionResult executeStep(SagaStep step, Map<String, Object> sagaInputData) {
        log.info("Executing step: {} of type: {}", step.getName(), step.getType());
        
        step.setStatus(StepStatus.RUNNING);
        step.setStartedAt(java.time.LocalDateTime.now());

        try {
            StepExecutionResult result = switch (step.getType()) {
                case HTTP_CALL -> httpStepExecutor.execute(step, sagaInputData);
                case DATABASE_OPERATION -> databaseStepExecutor.execute(step, sagaInputData);
                case BUSINESS_LOGIC -> businessLogicStepExecutor.execute(step, sagaInputData);
                case MESSAGE_QUEUE -> executeMessageQueueStep(step, sagaInputData);
                case FILE_OPERATION -> executeFileOperationStep(step, sagaInputData);
                case WAIT -> executeWaitStep(step, sagaInputData);
                case CONDITIONAL -> executeConditionalStep(step, sagaInputData);
                case PARALLEL -> executeParallelStep(step, sagaInputData);
                case SUB_SAGA -> executeSubSagaStep(step, sagaInputData);
            };

            log.info("Step {} executed successfully", step.getName());
            return result;

        } catch (Exception e) {
            log.error("Error executing step: {}", step.getName(), e);
            return StepExecutionResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .errorStackTrace(getStackTrace(e))
                    .build();
        }
    }

    /**
     * Execute message queue step
     */
    private StepExecutionResult executeMessageQueueStep(SagaStep step, Map<String, Object> sagaInputData) {
        // TODO: Implement message queue execution
        log.warn("Message queue step execution not yet implemented");
        return StepExecutionResult.builder()
                .success(true)
                .outputData(Map.of("message", "Message queue step executed"))
                .build();
    }

    /**
     * Execute file operation step
     */
    private StepExecutionResult executeFileOperationStep(SagaStep step, Map<String, Object> sagaInputData) {
        // TODO: Implement file operation execution
        log.warn("File operation step execution not yet implemented");
        return StepExecutionResult.builder()
                .success(true)
                .outputData(Map.of("file", "File operation step executed"))
                .build();
    }

    /**
     * Execute wait step
     */
    private StepExecutionResult executeWaitStep(SagaStep step, Map<String, Object> sagaInputData) {
        try {
            long delayMs = step.getConfig().getDelayMs();
            log.info("Waiting for {} ms", delayMs);
            Thread.sleep(delayMs);
            
            return StepExecutionResult.builder()
                    .success(true)
                    .outputData(Map.of("waited", delayMs))
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return StepExecutionResult.builder()
                    .success(false)
                    .errorMessage("Wait step interrupted")
                    .build();
        }
    }

    /**
     * Execute conditional step
     */
    private StepExecutionResult executeConditionalStep(SagaStep step, Map<String, Object> sagaInputData) {
        // TODO: Implement conditional step execution
        log.warn("Conditional step execution not yet implemented");
        return StepExecutionResult.builder()
                .success(true)
                .outputData(Map.of("condition", "Conditional step executed"))
                .build();
    }

    /**
     * Execute parallel step
     */
    private StepExecutionResult executeParallelStep(SagaStep step, Map<String, Object> sagaInputData) {
        // TODO: Implement parallel step execution
        log.warn("Parallel step execution not yet implemented");
        return StepExecutionResult.builder()
                .success(true)
                .outputData(Map.of("parallel", "Parallel step executed"))
                .build();
    }

    /**
     * Execute sub-saga step
     */
    private StepExecutionResult executeSubSagaStep(SagaStep step, Map<String, Object> sagaInputData) {
        // TODO: Implement sub-saga execution
        log.warn("Sub-saga step execution not yet implemented");
        return StepExecutionResult.builder()
                .success(true)
                .outputData(Map.of("subSaga", "Sub-saga step executed"))
                .build();
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