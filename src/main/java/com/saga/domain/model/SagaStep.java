package com.saga.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a single step within a Saga workflow.
 * Each step can be executed and compensated independently.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStep {

    /**
     * Unique identifier for the step
     */
    private String stepId;

    /**
     * Name of the step
     */
    private String name;

    /**
     * Description of what the step does
     */
    private String description;

    /**
     * Order of execution (0-based)
     */
    private int order;

    /**
     * Current status of the step
     */
    private StepStatus status;

    /**
     * Type of step (HTTP call, database operation, etc.)
     */
    private StepType type;

    /**
     * Configuration for the step execution
     */
    private StepConfig config;

    /**
     * Input data for the step
     */
    private Map<String, Object> inputData;

    /**
     * Output data from the step execution
     */
    private Map<String, Object> outputData;

    /**
     * Error details if step failed
     */
    private String errorMessage;

    /**
     * Stack trace if step failed
     */
    private String errorStackTrace;

    /**
     * Number of retries attempted
     */
    private int retryCount;

    /**
     * Maximum number of retries allowed
     */
    private int maxRetries;

    /**
     * Timeout for this step in milliseconds
     */
    private long timeoutMs;

    /**
     * When the step started execution
     */
    private LocalDateTime startedAt;

    /**
     * When the step completed
     */
    private LocalDateTime completedAt;

    /**
     * Duration of step execution in milliseconds
     */
    private Long durationMs;

    /**
     * Whether this step is required for saga completion
     */
    private boolean required;

    /**
     * Whether this step can be compensated
     */
    private boolean compensatable;

    /**
     * Compensation configuration
     */
    private CompensationConfig compensationConfig;

    /**
     * Custom metadata for the step
     */
    private Map<String, Object> metadata;

    /**
     * Check if step is in a final state
     */
    public boolean isCompleted() {
        return status == StepStatus.COMPLETED || 
               status == StepStatus.FAILED || 
               status == StepStatus.COMPENSATED;
    }

    /**
     * Check if step can be retried
     */
    public boolean canRetry() {
        return status == StepStatus.FAILED && retryCount < maxRetries;
    }

    /**
     * Check if step has timed out
     */
    public boolean hasTimedOut() {
        if (startedAt == null || timeoutMs <= 0) {
            return false;
        }
        return System.currentTimeMillis() - startedAt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli() > timeoutMs;
    }

    /**
     * Calculate execution duration
     */
    public void calculateDuration() {
        if (startedAt != null && completedAt != null) {
            this.durationMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }
} 