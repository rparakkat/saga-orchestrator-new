package com.saga.application.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Result of executing a saga step.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepExecutionResult {

    /**
     * Whether the step executed successfully
     */
    private boolean success;

    /**
     * Output data from the step execution
     */
    private Map<String, Object> outputData;

    /**
     * Error message if step failed
     */
    private String errorMessage;

    /**
     * Error stack trace if step failed
     */
    private String errorStackTrace;

    /**
     * Duration of step execution in milliseconds
     */
    private Long durationMs;
} 