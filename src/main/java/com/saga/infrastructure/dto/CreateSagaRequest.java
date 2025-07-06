package com.saga.infrastructure.dto;

import com.saga.domain.model.SagaStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a new saga.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSagaRequest {

    /**
     * Name of the saga
     */
    private String name;

    /**
     * List of steps in the saga
     */
    private List<SagaStep> steps;

    /**
     * Input data for the saga
     */
    private Map<String, Object> inputData;

    /**
     * Business correlation ID
     */
    private String correlationId;

    /**
     * Priority of the saga
     */
    private Integer priority;

    /**
     * Tags for categorization
     */
    private List<String> tags;

    /**
     * Custom metadata
     */
    private Map<String, Object> metadata;
} 