package com.saga.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Metrics service to track performance and system health for better scalability monitoring.
 */
@Slf4j
@Service
public class MetricsService {

    // Saga execution metrics
    private final LongAdder totalSagasExecuted = new LongAdder();
    private final LongAdder successfulSagas = new LongAdder();
    private final LongAdder failedSagas = new LongAdder();
    private final LongAdder compensatedSagas = new LongAdder();
    private final LongAdder timedOutSagas = new LongAdder();

    // Step execution metrics
    private final LongAdder totalStepsExecuted = new LongAdder();
    private final LongAdder successfulSteps = new LongAdder();
    private final LongAdder failedSteps = new LongAdder();
    private final LongAdder retriedSteps = new LongAdder();

    // Performance metrics
    private final Map<String, LongAdder> stepTypeExecutionCount = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> stepTypeFailureCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> stepTypeAverageExecutionTime = new ConcurrentHashMap<>();

    // Circuit breaker metrics
    private final Map<String, LongAdder> circuitBreakerTrips = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> circuitBreakerResets = new ConcurrentHashMap<>();

    // Rate limiting metrics
    private final LongAdder rateLimitExceededCount = new LongAdder();
    private final LongAdder totalRequests = new LongAdder();

    // System health metrics
    private final AtomicLong activeSagas = new AtomicLong(0);
    private final AtomicLong pendingSagas = new AtomicLong(0);
    private final AtomicLong systemStartTime = new AtomicLong(System.currentTimeMillis());

    /**
     * Record saga execution
     */
    public void recordSagaExecution(String sagaId, boolean success, long executionTimeMs) {
        totalSagasExecuted.increment();
        
        if (success) {
            successfulSagas.increment();
        } else {
            failedSagas.increment();
        }
        
        log.debug("Saga execution recorded: {} (success: {}, time: {}ms)", sagaId, success, executionTimeMs);
    }

    /**
     * Record saga compensation
     */
    public void recordSagaCompensation(String sagaId) {
        compensatedSagas.increment();
        log.debug("Saga compensation recorded: {}", sagaId);
    }

    /**
     * Record saga timeout
     */
    public void recordSagaTimeout(String sagaId) {
        timedOutSagas.increment();
        log.debug("Saga timeout recorded: {}", sagaId);
    }

    /**
     * Record step execution
     */
    public void recordStepExecution(String stepType, boolean success, long executionTimeMs) {
        totalStepsExecuted.increment();
        
        if (success) {
            successfulSteps.increment();
        } else {
            failedSteps.increment();
        }
        
        // Update step type metrics
        stepTypeExecutionCount.computeIfAbsent(stepType, k -> new LongAdder()).increment();
        if (!success) {
            stepTypeFailureCount.computeIfAbsent(stepType, k -> new LongAdder()).increment();
        }
        
        // Update average execution time
        AtomicLong avgTime = stepTypeAverageExecutionTime.computeIfAbsent(stepType, k -> new AtomicLong(0));
        long currentAvg = avgTime.get();
        long newAvg = (currentAvg + executionTimeMs) / 2; // Simple moving average
        avgTime.set(newAvg);
        
        log.debug("Step execution recorded: {} (success: {}, time: {}ms)", stepType, success, executionTimeMs);
    }

    /**
     * Record step retry
     */
    public void recordStepRetry(String stepType) {
        retriedSteps.increment();
        log.debug("Step retry recorded: {}", stepType);
    }

    /**
     * Record circuit breaker trip
     */
    public void recordCircuitBreakerTrip(String serviceName) {
        circuitBreakerTrips.computeIfAbsent(serviceName, k -> new LongAdder()).increment();
        log.debug("Circuit breaker trip recorded: {}", serviceName);
    }

    /**
     * Record circuit breaker reset
     */
    public void recordCircuitBreakerReset(String serviceName) {
        circuitBreakerResets.computeIfAbsent(serviceName, k -> new LongAdder()).increment();
        log.debug("Circuit breaker reset recorded: {}", serviceName);
    }

    /**
     * Record rate limit exceeded
     */
    public void recordRateLimitExceeded() {
        rateLimitExceededCount.increment();
        log.debug("Rate limit exceeded recorded");
    }

    /**
     * Record request
     */
    public void recordRequest() {
        totalRequests.increment();
    }

    /**
     * Update active sagas count
     */
    public void setActiveSagas(long count) {
        activeSagas.set(count);
    }

    /**
     * Update pending sagas count
     */
    public void setPendingSagas(long count) {
        pendingSagas.set(count);
    }

    /**
     * Get comprehensive metrics
     */
    public MetricsSnapshot getMetricsSnapshot() {
        return MetricsSnapshot.builder()
                .timestamp(LocalDateTime.now())
                .uptimeMs(System.currentTimeMillis() - systemStartTime.get())
                
                // Saga metrics
                .totalSagasExecuted(totalSagasExecuted.sum())
                .successfulSagas(successfulSagas.sum())
                .failedSagas(failedSagas.sum())
                .compensatedSagas(compensatedSagas.sum())
                .timedOutSagas(timedOutSagas.sum())
                .sagaSuccessRate(calculateSuccessRate(totalSagasExecuted.sum(), successfulSagas.sum()))
                
                // Step metrics
                .totalStepsExecuted(totalStepsExecuted.sum())
                .successfulSteps(successfulSteps.sum())
                .failedSteps(failedSteps.sum())
                .retriedSteps(retriedSteps.sum())
                .stepSuccessRate(calculateSuccessRate(totalStepsExecuted.sum(), successfulSteps.sum()))
                
                // System health
                .activeSagas(activeSagas.get())
                .pendingSagas(pendingSagas.get())
                
                // Rate limiting
                .totalRequests(totalRequests.sum())
                .rateLimitExceededCount(rateLimitExceededCount.sum())
                .rateLimitExceededRate(calculateRate(totalRequests.sum(), rateLimitExceededCount.sum()))
                
                // Step type breakdown
                .stepTypeMetrics(createStepTypeMetrics())
                
                // Circuit breaker metrics
                .circuitBreakerMetrics(createCircuitBreakerMetrics())
                
                .build();
    }

    /**
     * Calculate success rate
     */
    private double calculateSuccessRate(long total, long successful) {
        return total > 0 ? (double) successful / total * 100 : 0.0;
    }

    /**
     * Calculate rate
     */
    private double calculateRate(long total, long count) {
        return total > 0 ? (double) count / total * 100 : 0.0;
    }

    /**
     * Create step type metrics
     */
    private Map<String, StepTypeMetrics> createStepTypeMetrics() {
        Map<String, StepTypeMetrics> metrics = new ConcurrentHashMap<>();
        
        stepTypeExecutionCount.forEach((stepType, executionCount) -> {
            LongAdder failureCount = stepTypeFailureCount.get(stepType);
            AtomicLong avgTime = stepTypeAverageExecutionTime.get(stepType);
            
            metrics.put(stepType, StepTypeMetrics.builder()
                    .executionCount(executionCount.sum())
                    .failureCount(failureCount != null ? failureCount.sum() : 0)
                    .averageExecutionTimeMs(avgTime != null ? avgTime.get() : 0)
                    .successRate(calculateSuccessRate(executionCount.sum(), 
                            executionCount.sum() - (failureCount != null ? failureCount.sum() : 0)))
                    .build());
        });
        
        return metrics;
    }

    /**
     * Create circuit breaker metrics
     */
    private Map<String, CircuitBreakerMetrics> createCircuitBreakerMetrics() {
        Map<String, CircuitBreakerMetrics> metrics = new ConcurrentHashMap<>();
        
        circuitBreakerTrips.forEach((serviceName, trips) -> {
            LongAdder resets = circuitBreakerResets.get(serviceName);
            
            metrics.put(serviceName, CircuitBreakerMetrics.builder()
                    .trips(trips.sum())
                    .resets(resets != null ? resets.sum() : 0)
                    .build());
        });
        
        return metrics;
    }

    /**
     * Reset all metrics (for testing or maintenance)
     */
    public void resetMetrics() {
        totalSagasExecuted.reset();
        successfulSagas.reset();
        failedSagas.reset();
        compensatedSagas.reset();
        timedOutSagas.reset();
        
        totalStepsExecuted.reset();
        successfulSteps.reset();
        failedSteps.reset();
        retriedSteps.reset();
        
        stepTypeExecutionCount.clear();
        stepTypeFailureCount.clear();
        stepTypeAverageExecutionTime.clear();
        
        circuitBreakerTrips.clear();
        circuitBreakerResets.clear();
        
        rateLimitExceededCount.reset();
        totalRequests.reset();
        
        activeSagas.set(0);
        pendingSagas.set(0);
        
        log.info("All metrics have been reset");
    }

    /**
     * Metrics snapshot for reporting
     */
    public static class MetricsSnapshot {
        private final LocalDateTime timestamp;
        private final long uptimeMs;
        private final long totalSagasExecuted;
        private final long successfulSagas;
        private final long failedSagas;
        private final long compensatedSagas;
        private final long timedOutSagas;
        private final double sagaSuccessRate;
        private final long totalStepsExecuted;
        private final long successfulSteps;
        private final long failedSteps;
        private final long retriedSteps;
        private final double stepSuccessRate;
        private final long activeSagas;
        private final long pendingSagas;
        private final long totalRequests;
        private final long rateLimitExceededCount;
        private final double rateLimitExceededRate;
        private final Map<String, StepTypeMetrics> stepTypeMetrics;
        private final Map<String, CircuitBreakerMetrics> circuitBreakerMetrics;

        // Builder pattern implementation
        public static Builder builder() {
            return new Builder();
        }

        private MetricsSnapshot(Builder builder) {
            this.timestamp = builder.timestamp;
            this.uptimeMs = builder.uptimeMs;
            this.totalSagasExecuted = builder.totalSagasExecuted;
            this.successfulSagas = builder.successfulSagas;
            this.failedSagas = builder.failedSagas;
            this.compensatedSagas = builder.compensatedSagas;
            this.timedOutSagas = builder.timedOutSagas;
            this.sagaSuccessRate = builder.sagaSuccessRate;
            this.totalStepsExecuted = builder.totalStepsExecuted;
            this.successfulSteps = builder.successfulSteps;
            this.failedSteps = builder.failedSteps;
            this.retriedSteps = builder.retriedSteps;
            this.stepSuccessRate = builder.stepSuccessRate;
            this.activeSagas = builder.activeSagas;
            this.pendingSagas = builder.pendingSagas;
            this.totalRequests = builder.totalRequests;
            this.rateLimitExceededCount = builder.rateLimitExceededCount;
            this.rateLimitExceededRate = builder.rateLimitExceededRate;
            this.stepTypeMetrics = builder.stepTypeMetrics;
            this.circuitBreakerMetrics = builder.circuitBreakerMetrics;
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public long getUptimeMs() { return uptimeMs; }
        public long getTotalSagasExecuted() { return totalSagasExecuted; }
        public long getSuccessfulSagas() { return successfulSagas; }
        public long getFailedSagas() { return failedSagas; }
        public long getCompensatedSagas() { return compensatedSagas; }
        public long getTimedOutSagas() { return timedOutSagas; }
        public double getSagaSuccessRate() { return sagaSuccessRate; }
        public long getTotalStepsExecuted() { return totalStepsExecuted; }
        public long getSuccessfulSteps() { return successfulSteps; }
        public long getFailedSteps() { return failedSteps; }
        public long getRetriedSteps() { return retriedSteps; }
        public double getStepSuccessRate() { return stepSuccessRate; }
        public long getActiveSagas() { return activeSagas; }
        public long getPendingSagas() { return pendingSagas; }
        public long getTotalRequests() { return totalRequests; }
        public long getRateLimitExceededCount() { return rateLimitExceededCount; }
        public double getRateLimitExceededRate() { return rateLimitExceededRate; }
        public Map<String, StepTypeMetrics> getStepTypeMetrics() { return stepTypeMetrics; }
        public Map<String, CircuitBreakerMetrics> getCircuitBreakerMetrics() { return circuitBreakerMetrics; }

        public static class Builder {
            private LocalDateTime timestamp;
            private long uptimeMs;
            private long totalSagasExecuted;
            private long successfulSagas;
            private long failedSagas;
            private long compensatedSagas;
            private long timedOutSagas;
            private double sagaSuccessRate;
            private long totalStepsExecuted;
            private long successfulSteps;
            private long failedSteps;
            private long retriedSteps;
            private double stepSuccessRate;
            private long activeSagas;
            private long pendingSagas;
            private long totalRequests;
            private long rateLimitExceededCount;
            private double rateLimitExceededRate;
            private Map<String, StepTypeMetrics> stepTypeMetrics;
            private Map<String, CircuitBreakerMetrics> circuitBreakerMetrics;

            public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            public Builder uptimeMs(long uptimeMs) { this.uptimeMs = uptimeMs; return this; }
            public Builder totalSagasExecuted(long totalSagasExecuted) { this.totalSagasExecuted = totalSagasExecuted; return this; }
            public Builder successfulSagas(long successfulSagas) { this.successfulSagas = successfulSagas; return this; }
            public Builder failedSagas(long failedSagas) { this.failedSagas = failedSagas; return this; }
            public Builder compensatedSagas(long compensatedSagas) { this.compensatedSagas = compensatedSagas; return this; }
            public Builder timedOutSagas(long timedOutSagas) { this.timedOutSagas = timedOutSagas; return this; }
            public Builder sagaSuccessRate(double sagaSuccessRate) { this.sagaSuccessRate = sagaSuccessRate; return this; }
            public Builder totalStepsExecuted(long totalStepsExecuted) { this.totalStepsExecuted = totalStepsExecuted; return this; }
            public Builder successfulSteps(long successfulSteps) { this.successfulSteps = successfulSteps; return this; }
            public Builder failedSteps(long failedSteps) { this.failedSteps = failedSteps; return this; }
            public Builder retriedSteps(long retriedSteps) { this.retriedSteps = retriedSteps; return this; }
            public Builder stepSuccessRate(double stepSuccessRate) { this.stepSuccessRate = stepSuccessRate; return this; }
            public Builder activeSagas(long activeSagas) { this.activeSagas = activeSagas; return this; }
            public Builder pendingSagas(long pendingSagas) { this.pendingSagas = pendingSagas; return this; }
            public Builder totalRequests(long totalRequests) { this.totalRequests = totalRequests; return this; }
            public Builder rateLimitExceededCount(long rateLimitExceededCount) { this.rateLimitExceededCount = rateLimitExceededCount; return this; }
            public Builder rateLimitExceededRate(double rateLimitExceededRate) { this.rateLimitExceededRate = rateLimitExceededRate; return this; }
            public Builder stepTypeMetrics(Map<String, StepTypeMetrics> stepTypeMetrics) { this.stepTypeMetrics = stepTypeMetrics; return this; }
            public Builder circuitBreakerMetrics(Map<String, CircuitBreakerMetrics> circuitBreakerMetrics) { this.circuitBreakerMetrics = circuitBreakerMetrics; return this; }

            public MetricsSnapshot build() {
                return new MetricsSnapshot(this);
            }
        }
    }

    /**
     * Step type metrics
     */
    public static class StepTypeMetrics {
        private final long executionCount;
        private final long failureCount;
        private final long averageExecutionTimeMs;
        private final double successRate;

        private StepTypeMetrics(Builder builder) {
            this.executionCount = builder.executionCount;
            this.failureCount = builder.failureCount;
            this.averageExecutionTimeMs = builder.averageExecutionTimeMs;
            this.successRate = builder.successRate;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public long getExecutionCount() { return executionCount; }
        public long getFailureCount() { return failureCount; }
        public long getAverageExecutionTimeMs() { return averageExecutionTimeMs; }
        public double getSuccessRate() { return successRate; }

        public static class Builder {
            private long executionCount;
            private long failureCount;
            private long averageExecutionTimeMs;
            private double successRate;

            public Builder executionCount(long executionCount) { this.executionCount = executionCount; return this; }
            public Builder failureCount(long failureCount) { this.failureCount = failureCount; return this; }
            public Builder averageExecutionTimeMs(long averageExecutionTimeMs) { this.averageExecutionTimeMs = averageExecutionTimeMs; return this; }
            public Builder successRate(double successRate) { this.successRate = successRate; return this; }

            public StepTypeMetrics build() {
                return new StepTypeMetrics(this);
            }
        }
    }

    /**
     * Circuit breaker metrics
     */
    public static class CircuitBreakerMetrics {
        private final long trips;
        private final long resets;

        private CircuitBreakerMetrics(Builder builder) {
            this.trips = builder.trips;
            this.resets = builder.resets;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public long getTrips() { return trips; }
        public long getResets() { return resets; }

        public static class Builder {
            private long trips;
            private long resets;

            public Builder trips(long trips) { this.trips = trips; return this; }
            public Builder resets(long resets) { this.resets = resets; return this; }

            public CircuitBreakerMetrics build() {
                return new CircuitBreakerMetrics(this);
            }
        }
    }
} 