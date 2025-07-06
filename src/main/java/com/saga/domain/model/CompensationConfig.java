package com.saga.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Configuration for compensating (rolling back) a Saga Step.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationConfig {

    /**
     * Type of compensation action
     */
    private CompensationType type;

    /**
     * For HTTP compensation: the URL to call
     */
    private String url;

    /**
     * For HTTP compensation: the HTTP method
     */
    private String httpMethod;

    /**
     * For HTTP compensation: request headers
     */
    private Map<String, String> headers;

    /**
     * For HTTP compensation: request body template
     */
    private String requestBodyTemplate;

    /**
     * For database compensation: the query to execute
     */
    private String query;

    /**
     * For database compensation: query parameters
     */
    private Map<String, Object> queryParameters;

    /**
     * For business logic compensation: class name
     */
    private String className;

    /**
     * For business logic compensation: method name
     */
    private String methodName;

    /**
     * For message queue compensation: queue name
     */
    private String queueName;

    /**
     * For message queue compensation: message template
     */
    private String messageTemplate;

    /**
     * Custom compensation properties
     */
    private Map<String, Object> properties;

    /**
     * Timeout for compensation in milliseconds
     */
    private long timeoutMs;

    /**
     * Maximum number of retries for compensation
     */
    private int maxRetries;

    /**
     * Retry delay in milliseconds
     */
    private long retryDelayMs;

    /**
     * Whether compensation is required for saga consistency
     */
    private boolean required;

    /**
     * Order of compensation (reverse order of execution)
     */
    private int compensationOrder;
} 