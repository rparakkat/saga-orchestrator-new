package com.saga.infrastructure.controller;

import com.saga.application.service.SagaOrchestratorService;
import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStatus;
import com.saga.infrastructure.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST controller for dashboard data and statistics.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SagaOrchestratorService sagaOrchestratorService;
    private final MetricsService metricsService;

    /**
     * Get dashboard overview statistics
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        log.info("Retrieving dashboard overview");
        
        try {
            MetricsService.MetricsSnapshot metrics = metricsService.getMetricsSnapshot();
            
            Map<String, Object> overview = new HashMap<>();
            overview.put("totalSagas", metrics.getTotalSagasExecuted());
            overview.put("successfulSagas", metrics.getSuccessfulSagas());
            overview.put("failedSagas", metrics.getFailedSagas());
            overview.put("compensatedSagas", metrics.getCompensatedSagas());
            overview.put("timedOutSagas", metrics.getTimedOutSagas());
            overview.put("sagaSuccessRate", metrics.getSagaSuccessRate());
            overview.put("stepSuccessRate", metrics.getStepSuccessRate());
            overview.put("activeSagas", metrics.getActiveSagas());
            overview.put("pendingSagas", metrics.getPendingSagas());
            overview.put("totalRequests", metrics.getTotalRequests());
            overview.put("rateLimitExceededRate", metrics.getRateLimitExceededRate());
            overview.put("uptimeMs", metrics.getUptimeMs());
            overview.put("timestamp", metrics.getTimestamp());
            
            return ResponseEntity.ok(overview);
            
        } catch (Exception e) {
            log.error("Error retrieving dashboard overview", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve dashboard overview"));
        }
    }

    /**
     * Get recent sagas for dashboard
     */
    @GetMapping("/recent-sagas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Saga>> getRecentSagas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Retrieving recent sagas, page: {}, size: {}", page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            // This would need to be implemented in the repository
            // For now, return empty page
            return ResponseEntity.ok(Page.empty(pageable));
            
        } catch (Exception e) {
            log.error("Error retrieving recent sagas", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get sagas by status for dashboard
     */
    @GetMapping("/sagas-by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSagasByStatus() {
        log.info("Retrieving sagas by status");
        
        try {
            Map<String, Object> statusCounts = Map.of(
                "running", sagaOrchestratorService.getSagasByStatus(SagaStatus.RUNNING).size(),
                "completed", sagaOrchestratorService.getSagasByStatus(SagaStatus.COMPLETED).size(),
                "failed", sagaOrchestratorService.getSagasByStatus(SagaStatus.FAILED).size(),
                "compensated", sagaOrchestratorService.getSagasByStatus(SagaStatus.COMPENSATED).size(),
                "timeout", sagaOrchestratorService.getSagasByStatus(SagaStatus.TIMEOUT).size()
            );
            
            return ResponseEntity.ok(statusCounts);
            
        } catch (Exception e) {
            log.error("Error retrieving sagas by status", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve sagas by status"));
        }
    }

    /**
     * Get step type metrics for dashboard
     */
    @GetMapping("/step-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStepMetrics() {
        log.info("Retrieving step metrics");
        
        try {
            MetricsService.MetricsSnapshot metrics = metricsService.getMetricsSnapshot();
            return ResponseEntity.ok(Map.of("stepTypeMetrics", metrics.getStepTypeMetrics()));
            
        } catch (Exception e) {
            log.error("Error retrieving step metrics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve step metrics"));
        }
    }

    /**
     * Get circuit breaker metrics for dashboard
     */
    @GetMapping("/circuit-breaker-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerMetrics() {
        log.info("Retrieving circuit breaker metrics");
        
        try {
            MetricsService.MetricsSnapshot metrics = metricsService.getMetricsSnapshot();
            return ResponseEntity.ok(Map.of("circuitBreakerMetrics", metrics.getCircuitBreakerMetrics()));
            
        } catch (Exception e) {
            log.error("Error retrieving circuit breaker metrics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve circuit breaker metrics"));
        }
    }

    /**
     * Get performance metrics for dashboard
     */
    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        log.info("Retrieving performance metrics");
        
        try {
            MetricsService.MetricsSnapshot metrics = metricsService.getMetricsSnapshot();
            
            Map<String, Object> performance = Map.of(
                "totalStepsExecuted", metrics.getTotalStepsExecuted(),
                "successfulSteps", metrics.getSuccessfulSteps(),
                "failedSteps", metrics.getFailedSteps(),
                "retriedSteps", metrics.getRetriedSteps(),
                "stepSuccessRate", metrics.getStepSuccessRate(),
                "totalRequests", metrics.getTotalRequests(),
                "rateLimitExceededCount", metrics.getRateLimitExceededCount(),
                "rateLimitExceededRate", metrics.getRateLimitExceededRate(),
                "uptimeMs", metrics.getUptimeMs()
            );
            
            return ResponseEntity.ok(performance);
            
        } catch (Exception e) {
            log.error("Error retrieving performance metrics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve performance metrics"));
        }
    }

    /**
     * Get system health for dashboard
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        log.info("Retrieving system health");
        
        try {
            MetricsService.MetricsSnapshot metrics = metricsService.getMetricsSnapshot();
            
            // Calculate health status based on metrics
            String status = "HEALTHY";
            String message = "System is operating normally";
            
            if (metrics.getSagaSuccessRate() < 90.0) {
                status = "WARNING";
                message = "Saga success rate is below 90%";
            }
            
            if (metrics.getStepSuccessRate() < 85.0) {
                status = "WARNING";
                message = "Step success rate is below 85%";
            }
            
            if (metrics.getRateLimitExceededRate() > 10.0) {
                status = "WARNING";
                message = "High rate limiting activity detected";
            }
            
            Map<String, Object> health = Map.of(
                "status", status,
                "message", message,
                "timestamp", LocalDateTime.now(),
                "metrics", metrics
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Error retrieving system health", e);
            return ResponseEntity.ok(Map.of(
                "status", "UNKNOWN",
                "message", "Unable to determine system health",
                "timestamp", LocalDateTime.now()
            ));
        }
    }
} 