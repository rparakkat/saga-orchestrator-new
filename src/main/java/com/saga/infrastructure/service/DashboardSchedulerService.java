package com.saga.infrastructure.service;

import com.saga.infrastructure.websocket.SagaWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for dashboard updates and maintenance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardSchedulerService {

    private final SagaWebSocketHandler webSocketHandler;
    private final MetricsService metricsService;

    /**
     * Broadcast metrics update every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void broadcastMetricsUpdate() {
        try {
            webSocketHandler.broadcastMetricsUpdate();
        } catch (Exception e) {
            log.error("Error broadcasting metrics update", e);
        }
    }

    /**
     * Clean up old metrics data every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupOldMetrics() {
        try {
            log.info("Cleaning up old metrics data");
            // This would clean up old metrics data if needed
            // For now, just log the cleanup
        } catch (Exception e) {
            log.error("Error cleaning up old metrics", e);
        }
    }

    /**
     * Health check broadcast every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void broadcastHealthCheck() {
        try {
            // Broadcast system health status
            log.debug("Broadcasting health check");
            // This could include system health metrics
        } catch (Exception e) {
            log.error("Error broadcasting health check", e);
        }
    }
} 