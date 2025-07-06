package com.saga.application.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a compensation operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationResult {

    /**
     * Whether the compensation was successful
     */
    private boolean success;

    /**
     * Success message
     */
    private String message;

    /**
     * Error message if compensation failed
     */
    private String errorMessage;

    /**
     * Error stack trace if compensation failed
     */
    private String errorStackTrace;
} 