package com.saga.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.domain.event.SagaEvent;
import com.saga.infrastructure.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket handler for real-time saga event streaming to the dashboard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket connection established: {}", sessionId);
        
        // Send initial metrics
        sendInitialMetrics(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        log.info("WebSocket connection closed: {} with status: {}", sessionId, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.debug("Received WebSocket message: {}", payload);
            
            // Handle dashboard commands
            DashboardCommand command = objectMapper.readValue(payload, DashboardCommand.class);
            handleDashboardCommand(session, command);
            
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    /**
     * Broadcast saga event to all connected clients
     */
    public void broadcastSagaEvent(SagaEvent event) {
        DashboardMessage message = DashboardMessage.builder()
                .type("SAGA_EVENT")
                .data(event)
                .timestamp(System.currentTimeMillis())
                .build();
        
        broadcastMessage(message);
    }

    /**
     * Broadcast metrics update to all connected clients
     */
    public void broadcastMetricsUpdate() {
        try {
            MetricsService.MetricsSnapshot metrics = metricsService.getMetricsSnapshot();
            
            DashboardMessage message = DashboardMessage.builder()
                    .type("METRICS_UPDATE")
                    .data(metrics)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            broadcastMessage(message);
            
        } catch (Exception e) {
            log.error("Error broadcasting metrics update", e);
        }
    }

    /**
     * Send initial metrics to a specific session
     */
    private void sendInitialMetrics(WebSocketSession session) {
        try {
            MetricsService.MetricsSnapshot metrics = metricsService.getMetricsSnapshot();
            
            DashboardMessage message = DashboardMessage.builder()
                    .type("INITIAL_METRICS")
                    .data(metrics)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            sendMessage(session, message);
            
        } catch (Exception e) {
            log.error("Error sending initial metrics", e);
        }
    }

    /**
     * Handle dashboard commands from clients
     */
    private void handleDashboardCommand(WebSocketSession session, DashboardCommand command) {
        try {
            switch (command.getType()) {
                case "SUBSCRIBE_METRICS":
                    // Start sending periodic metrics updates
                    startMetricsSubscription(session);
                    break;
                case "UNSUBSCRIBE_METRICS":
                    // Stop sending periodic metrics updates
                    stopMetricsSubscription(session);
                    break;
                case "REQUEST_SAGA_DETAILS":
                    // Send specific saga details
                    sendSagaDetails(session, command.getSagaId());
                    break;
                default:
                    log.warn("Unknown dashboard command: {}", command.getType());
            }
        } catch (Exception e) {
            log.error("Error handling dashboard command", e);
        }
    }

    /**
     * Start periodic metrics updates for a session
     */
    private void startMetricsSubscription(WebSocketSession session) {
        // Schedule periodic metrics updates every 5 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    broadcastMetricsUpdate();
                }
            } catch (Exception e) {
                log.error("Error in metrics subscription", e);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Stop periodic metrics updates for a session
     */
    private void stopMetricsSubscription(WebSocketSession session) {
        // Implementation would track and stop specific subscriptions
        log.info("Stopped metrics subscription for session: {}", session.getId());
    }

    /**
     * Send saga details to a specific session
     */
    private void sendSagaDetails(WebSocketSession session, String sagaId) {
        // This would fetch saga details from the repository
        // For now, send a placeholder message
        DashboardMessage message = DashboardMessage.builder()
                .type("SAGA_DETAILS")
                .data(Map.of("sagaId", sagaId, "message", "Saga details requested"))
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendMessage(session, message);
    }

    /**
     * Broadcast message to all connected clients
     */
    private void broadcastMessage(DashboardMessage message) {
        sessions.values().forEach(session -> sendMessage(session, message));
    }

    /**
     * Send message to a specific session
     */
    private void sendMessage(WebSocketSession session, DashboardMessage message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (IOException e) {
            log.error("Error sending WebSocket message", e);
        }
    }

    /**
     * Dashboard command from client
     */
    public static class DashboardCommand {
        private String type;
        private String sagaId;
        private Map<String, Object> data;

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSagaId() { return sagaId; }
        public void setSagaId(String sagaId) { this.sagaId = sagaId; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }

    /**
     * Dashboard message to client
     */
    public static class DashboardMessage {
        private String type;
        private Object data;
        private long timestamp;

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        private DashboardMessage(Builder builder) {
            this.type = builder.type;
            this.data = builder.data;
            this.timestamp = builder.timestamp;
        }

        // Getters
        public String getType() { return type; }
        public Object getData() { return data; }
        public long getTimestamp() { return timestamp; }

        public static class Builder {
            private String type;
            private Object data;
            private long timestamp;

            public Builder type(String type) { this.type = type; return this; }
            public Builder data(Object data) { this.data = data; return this; }
            public Builder timestamp(long timestamp) { this.timestamp = timestamp; return this; }

            public DashboardMessage build() {
                return new DashboardMessage(this);
            }
        }
    }
} 