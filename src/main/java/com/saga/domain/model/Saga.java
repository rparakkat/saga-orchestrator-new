package com.saga.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a Saga - a distributed transaction that consists of multiple steps.
 * Each step can either succeed or fail, and compensation actions are executed
 * for failed steps to maintain data consistency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sagas")
public class Saga {

    @Id
    private String id;

    /**
     * Unique identifier for the saga instance
     */
    private String sagaId;

    /**
     * Name/type of the saga workflow
     */
    private String name;

    /**
     * Current status of the saga
     */
    private SagaStatus status;

    /**
     * List of steps in the saga
     */
    private List<SagaStep> steps;

    /**
     * Current step index being executed
     */
    private int currentStepIndex;

    /**
     * Input data for the saga
     */
    private Map<String, Object> inputData;

    /**
     * Output data from the saga execution
     */
    private Map<String, Object> outputData;

    /**
     * Error details if saga failed
     */
    private String errorMessage;

    /**
     * Stack trace if saga failed
     */
    private String errorStackTrace;

    /**
     * Retry count for the current step
     */
    private int retryCount;

    /**
     * Maximum number of retries allowed
     */
    private int maxRetries;

    /**
     * Timeout for the entire saga in milliseconds
     */
    private long timeoutMs;

    /**
     * When the saga was created
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * When the saga was last updated
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * When the saga started execution
     */
    private LocalDateTime startedAt;

    /**
     * When the saga completed (successfully or with failure)
     */
    private LocalDateTime completedAt;

    /**
     * Version for optimistic locking
     */
    @Version
    private Long version;

    /**
     * Business correlation ID for tracking
     */
    private String correlationId;

    /**
     * Priority of the saga (higher number = higher priority)
     */
    private int priority;

    /**
     * Tags for categorization and filtering
     */
    private List<String> tags;

    /**
     * Custom metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if saga is in a final state
     */
    public boolean isCompleted() {
        return status == SagaStatus.COMPLETED || 
               status == SagaStatus.FAILED || 
               status == SagaStatus.COMPENSATED;
    }

    /**
     * Check if saga can be retried
     */
    public boolean canRetry() {
        return status == SagaStatus.FAILED && retryCount < maxRetries;
    }

    /**
     * Get current step
     */
    public SagaStep getCurrentStep() {
        if (steps != null && currentStepIndex >= 0 && currentStepIndex < steps.size()) {
            return steps.get(currentStepIndex);
        }
        return null;
    }

    /**
     * Check if saga has more steps to execute
     */
    public boolean hasMoreSteps() {
        return steps != null && currentStepIndex < steps.size() - 1;
    }

    /**
     * Move to next step
     */
    public void moveToNextStep() {
        if (hasMoreSteps()) {
            currentStepIndex++;
            retryCount = 0; // Reset retry count for new step
        }
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }
} 