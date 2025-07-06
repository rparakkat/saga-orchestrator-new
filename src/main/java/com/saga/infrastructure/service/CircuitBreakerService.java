package com.saga.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Circuit breaker service to handle external service failures gracefully.
 */
@Slf4j
@Service
public class CircuitBreakerService {

    @Value("${saga.circuit-breaker.failure-threshold:5}")
    private int failureThreshold;

    @Value("${saga.circuit-breaker.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${saga.circuit-breaker.success-threshold:3}")
    private int successThreshold;

    private final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();

    /**
     * Execute operation with circuit breaker protection
     */
    public <T> T execute(String serviceName, Supplier<T> operation) {
        CircuitBreakerState state = getOrCreateCircuitBreaker(serviceName);
        
        if (state.isOpen()) {
            log.warn("Circuit breaker is OPEN for service: {}", serviceName);
            throw new CircuitBreakerOpenException("Circuit breaker is open for service: " + serviceName);
        }
        
        try {
            T result = operation.get();
            state.recordSuccess();
            return result;
        } catch (Exception e) {
            state.recordFailure();
            throw e;
        }
    }

    /**
     * Execute operation with circuit breaker protection (void version)
     */
    public void execute(String serviceName, Runnable operation) {
        execute(serviceName, () -> {
            operation.run();
            return null;
        });
    }

    /**
     * Get circuit breaker status for a service
     */
    public CircuitBreakerStatus getStatus(String serviceName) {
        CircuitBreakerState state = circuitBreakers.get(serviceName);
        if (state == null) {
            return new CircuitBreakerStatus("CLOSED", 0, 0, 0, 0);
        }
        return state.getStatus();
    }

    /**
     * Manually reset circuit breaker for a service
     */
    public void resetCircuitBreaker(String serviceName) {
        CircuitBreakerState state = circuitBreakers.get(serviceName);
        if (state != null) {
            state.reset();
            log.info("Circuit breaker manually reset for service: {}", serviceName);
        }
    }

    /**
     * Get or create circuit breaker state for a service
     */
    private CircuitBreakerState getOrCreateCircuitBreaker(String serviceName) {
        return circuitBreakers.computeIfAbsent(serviceName, k -> new CircuitBreakerState());
    }

    /**
     * Circuit breaker state for a service
     */
    private class CircuitBreakerState {
        private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicReference<LocalDateTime> lastFailureTime = new AtomicReference<>();

        public boolean isOpen() {
            State currentState = state.get();
            
            if (currentState == State.OPEN) {
                // Check if timeout has passed
                LocalDateTime lastFailure = lastFailureTime.get();
                if (lastFailure != null && 
                    LocalDateTime.now().isAfter(lastFailure.plusSeconds(timeoutSeconds))) {
                    // Transition to HALF_OPEN
                    if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                        log.info("Circuit breaker transitioning from OPEN to HALF_OPEN");
                        successCount.set(0);
                    }
                }
            }
            
            return state.get() == State.OPEN;
        }

        public void recordSuccess() {
            State currentState = state.get();
            
            if (currentState == State.HALF_OPEN) {
                int success = successCount.incrementAndGet();
                if (success >= successThreshold) {
                    if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                        log.info("Circuit breaker transitioning from HALF_OPEN to CLOSED");
                        failureCount.set(0);
                        successCount.set(0);
                    }
                }
            } else if (currentState == State.CLOSED) {
                // Reset failure count on success
                failureCount.set(0);
            }
        }

        public void recordFailure() {
            State currentState = state.get();
            
            if (currentState == State.CLOSED) {
                int failures = failureCount.incrementAndGet();
                lastFailureTime.set(LocalDateTime.now());
                
                if (failures >= failureThreshold) {
                    if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                        log.warn("Circuit breaker transitioning from CLOSED to OPEN after {} failures", failures);
                    }
                }
            } else if (currentState == State.HALF_OPEN) {
                // Any failure in HALF_OPEN state goes back to OPEN
                if (state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                    log.warn("Circuit breaker transitioning from HALF_OPEN to OPEN after failure");
                    lastFailureTime.set(LocalDateTime.now());
                }
            }
        }

        public void reset() {
            state.set(State.CLOSED);
            failureCount.set(0);
            successCount.set(0);
            lastFailureTime.set(null);
        }

        public CircuitBreakerStatus getStatus() {
            return new CircuitBreakerStatus(
                    state.get().name(),
                    failureCount.get(),
                    successCount.get(),
                    failureThreshold,
                    successThreshold
            );
        }
    }

    /**
     * Circuit breaker states
     */
    private enum State {
        CLOSED,     // Normal operation
        OPEN,       // Failing, reject requests
        HALF_OPEN   // Testing if service is back
    }

    /**
     * Circuit breaker status response
     */
    public static class CircuitBreakerStatus {
        private final String state;
        private final int failureCount;
        private final int successCount;
        private final int failureThreshold;
        private final int successThreshold;

        public CircuitBreakerStatus(String state, int failureCount, int successCount,
                                  int failureThreshold, int successThreshold) {
            this.state = state;
            this.failureCount = failureCount;
            this.successCount = successCount;
            this.failureThreshold = failureThreshold;
            this.successThreshold = successThreshold;
        }

        // Getters
        public String getState() { return state; }
        public int getFailureCount() { return failureCount; }
        public int getSuccessCount() { return successCount; }
        public int getFailureThreshold() { return failureThreshold; }
        public int getSuccessThreshold() { return successThreshold; }
    }

    /**
     * Exception thrown when circuit breaker is open
     */
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
} 