package com.saga.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Configuration for a Saga Step execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepConfig {

    /**
     * For HTTP calls: the URL to call
     */
    private String url;

    /**
     * For HTTP calls: the HTTP method (GET, POST, PUT, DELETE)
     */
    private String httpMethod;

    /**
     * For HTTP calls: request headers
     */
    private Map<String, String> headers;

    /**
     * For HTTP calls: request body template
     */
    private String requestBodyTemplate;

    /**
     * For HTTP calls: expected response status codes
     */
    private int[] expectedStatusCodes;

    /**
     * For database operations: the query to execute
     */
    private String query;

    /**
     * For database operations: query parameters
     */
    private Map<String, Object> queryParameters;

    /**
     * For message queue: queue name
     */
    private String queueName;

    /**
     * For message queue: message template
     */
    private String messageTemplate;

    /**
     * For file operations: file path
     */
    private String filePath;

    /**
     * For file operations: operation type (READ, WRITE, DELETE)
     */
    private String fileOperation;

    /**
     * For business logic: class name to instantiate
     */
    private String className;

    /**
     * For business logic: method name to call
     */
    private String methodName;

    /**
     * For wait operations: delay in milliseconds
     */
    private long delayMs;

    /**
     * For conditional steps: condition expression
     */
    private String condition;

    /**
     * For parallel steps: list of parallel step IDs
     */
    private String[] parallelStepIds;

    /**
     * For sub-saga: saga name to execute
     */
    private String subSagaName;

    /**
     * Custom configuration properties
     */
    private Map<String, Object> properties;

    /**
     * Timeout for this step in milliseconds
     */
    private long timeoutMs;

    /**
     * Maximum number of retries for this step
     */
    private int maxRetries;

    /**
     * Retry delay in milliseconds
     */
    private long retryDelayMs;

    /**
     * Whether to continue saga execution if this step fails
     */
    private boolean continueOnFailure;

    /**
     * Whether this step is required for saga completion
     */
    private boolean required;
} 