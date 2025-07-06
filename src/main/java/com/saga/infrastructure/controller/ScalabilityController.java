package com.saga.infrastructure.controller;

import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStatus;
import com.saga.infrastructure.service.BulkOperationsService;
import com.saga.infrastructure.service.CircuitBreakerService;
import com.saga.infrastructure.service.MetricsService;
import com.saga.infrastructure.service.RateLimitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for scalability features and system management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/scalability")
@RequiredArgsConstructor
public class ScalabilityController {

    private final BulkOperationsService bulkOperationsService;
    private final MetricsService metricsService;
    private final CircuitBreakerService circuitBreakerService;
    private final RateLimitingService rateLimitingService;

    /**
     * Get system metrics
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetricsService.MetricsSnapshot> getMetrics() {
        log.info("Retrieving system metrics");
        return ResponseEntity.ok(metricsService.getMetricsSnapshot());
    }

    /**
     * Bulk update saga statuses
     */
    @PostMapping("/bulk/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkUpdateStatus(
            @RequestParam List<String> sagaIds,
            @RequestParam SagaStatus newStatus) {
        
        log.info("Bulk updating {} sagas to status: {}", sagaIds.size(), newStatus);
        
        CompletableFuture<Long> future = bulkOperationsService.bulkUpdateSagaStatuses(sagaIds, newStatus);
        
        return ResponseEntity.accepted().body(Map.of(
                "message", "Bulk update initiated",
                "sagaCount", sagaIds.size(),
                "newStatus", newStatus,
                "operationId", System.currentTimeMillis()
        ));
    }

    /**
     * Bulk retry failed sagas
     */
    @PostMapping("/bulk/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkRetryFailedSagas() {
        log.info("Bulk retrying failed sagas");
        
        CompletableFuture<Long> future = bulkOperationsService.bulkRetryFailedSagas();
        
        return ResponseEntity.accepted().body(Map.of(
                "message", "Bulk retry initiated",
                "operationId", System.currentTimeMillis()
        ));
    }

    /**
     * Bulk timeout long-running sagas
     */
    @PostMapping("/bulk/timeout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkTimeoutLongRunningSagas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeoutThreshold) {
        
        log.info("Bulk timing out sagas running longer than: {}", timeoutThreshold);
        
        CompletableFuture<Long> future = bulkOperationsService.bulkTimeoutLongRunningSagas(timeoutThreshold);
        
        return ResponseEntity.accepted().body(Map.of(
                "message", "Bulk timeout initiated",
                "timeoutThreshold", timeoutThreshold,
                "operationId", System.currentTimeMillis()
        ));
    }

    /**
     * Bulk delete old sagas
     */
    @DeleteMapping("/bulk/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkDeleteOldSagas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {
        
        log.info("Bulk deleting sagas older than: {}", cutoffDate);
        
        CompletableFuture<Long> future = bulkOperationsService.bulkDeleteOldSagas(cutoffDate);
        
        return ResponseEntity.accepted().body(Map.of(
                "message", "Bulk cleanup initiated",
                "cutoffDate", cutoffDate,
                "operationId", System.currentTimeMillis()
        ));
    }

    /**
     * Get sagas for bulk processing
     */
    @GetMapping("/bulk/sagas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Saga>> getSagasForBulkProcessing(
            @RequestParam SagaStatus status,
            Pageable pageable) {
        
        log.info("Getting sagas for bulk processing with status: {}", status);
        
        Page<Saga> sagas = bulkOperationsService.getSagasForBulkProcessing(status, pageable);
        
        return ResponseEntity.ok(sagas);
    }

    /**
     * Get circuit breaker status for a service
     */
    @GetMapping("/circuit-breaker/{serviceName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CircuitBreakerService.CircuitBreakerStatus> getCircuitBreakerStatus(
            @PathVariable String serviceName) {
        
        log.info("Getting circuit breaker status for service: {}", serviceName);
        
        CircuitBreakerService.CircuitBreakerStatus status = circuitBreakerService.getStatus(serviceName);
        
        return ResponseEntity.ok(status);
    }

    /**
     * Reset circuit breaker for a service
     */
    @PostMapping("/circuit-breaker/{serviceName}/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resetCircuitBreaker(@PathVariable String serviceName) {
        log.info("Resetting circuit breaker for service: {}", serviceName);
        
        circuitBreakerService.resetCircuitBreaker(serviceName);
        
        return ResponseEntity.ok(Map.of(
                "message", "Circuit breaker reset successfully",
                "serviceName", serviceName
        ));
    }

    /**
     * Get rate limit status for a client
     */
    @GetMapping("/rate-limit/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateLimitingService.RateLimitStatus> getRateLimitStatus(@PathVariable String clientId) {
        log.info("Getting rate limit status for client: {}", clientId);
        
        RateLimitingService.RateLimitStatus status = rateLimitingService.getRateLimitStatus(clientId);
        
        return ResponseEntity.ok(status);
    }

    /**
     * Reset rate limit for a client
     */
    @PostMapping("/rate-limit/{clientId}/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resetRateLimit(@PathVariable String clientId) {
        log.info("Resetting rate limit for client: {}", clientId);
        
        rateLimitingService.resetRateLimit(clientId);
        
        return ResponseEntity.ok(Map.of(
                "message", "Rate limit reset successfully",
                "clientId", clientId
        ));
    }

    /**
     * Reset all metrics
     */
    @PostMapping("/metrics/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resetMetrics() {
        log.info("Resetting all metrics");
        
        metricsService.resetMetrics();
        
        return ResponseEntity.ok(Map.of(
                "message", "All metrics reset successfully"
        ));
    }

    /**
     * Get system health summary
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        log.info("Getting system health summary");
        
        MetricsService.MetricsSnapshot metrics = metricsService.getMetricsSnapshot();
        
        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", metrics.getTimestamp(),
                "uptimeMs", metrics.getUptimeMs(),
                "activeSagas", metrics.getActiveSagas(),
                "pendingSagas", metrics.getPendingSagas(),
                "sagaSuccessRate", metrics.getSagaSuccessRate(),
                "stepSuccessRate", metrics.getStepSuccessRate(),
                "totalRequests", metrics.getTotalRequests(),
                "rateLimitExceededRate", metrics.getRateLimitExceededRate()
        );
        
        return ResponseEntity.ok(health);
    }
} 