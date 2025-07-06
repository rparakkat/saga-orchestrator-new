package com.saga.infrastructure.dto;

import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStatus;
import com.saga.domain.model.SagaStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for saga operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaResponse {

    /**
     * Saga ID
     */
    private String sagaId;

    /**
     * Name of the saga
     */
    private String name;

    /**
     * Current status
     */
    private SagaStatus status;

    /**
     * List of steps
     */
    private List<SagaStep> steps;

    /**
     * Current step index
     */
    private Integer currentStepIndex;

    /**
     * Input data
     */
    private Map<String, Object> inputData;

    /**
     * Output data
     */
    private Map<String, Object> outputData;

    /**
     * Error message if failed
     */
    private String errorMessage;

    /**
     * Retry count
     */
    private Integer retryCount;

    /**
     * Maximum retries
     */
    private Integer maxRetries;

    /**
     * Timeout in milliseconds
     */
    private Long timeoutMs;

    /**
     * Created timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Updated timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Started timestamp
     */
    private LocalDateTime startedAt;

    /**
     * Completed timestamp
     */
    private LocalDateTime completedAt;

    /**
     * Correlation ID
     */
    private String correlationId;

    /**
     * Priority
     */
    private Integer priority;

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Metadata
     */
    private Map<String, Object> metadata;

    /**
     * Convert Saga entity to SagaResponse
     */
    public static SagaResponse fromSaga(Saga saga) {
        return SagaResponse.builder()
                .sagaId(saga.getSagaId())
                .name(saga.getName())
                .status(saga.getStatus())
                .steps(saga.getSteps())
                .currentStepIndex(saga.getCurrentStepIndex())
                .inputData(saga.getInputData())
                .outputData(saga.getOutputData())
                .errorMessage(saga.getErrorMessage())
                .retryCount(saga.getRetryCount())
                .maxRetries(saga.getMaxRetries())
                .timeoutMs(saga.getTimeoutMs())
                .createdAt(saga.getCreatedAt())
                .updatedAt(saga.getUpdatedAt())
                .startedAt(saga.getStartedAt())
                .completedAt(saga.getCompletedAt())
                .correlationId(saga.getCorrelationId())
                .priority(saga.getPriority())
                .tags(saga.getTags())
                .metadata(saga.getMetadata())
                .build();
    }
} 