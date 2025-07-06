package com.saga.infrastructure.config;

import com.saga.infrastructure.websocket.SagaWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for real-time dashboard communication.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SagaWebSocketHandler sagaWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sagaWebSocketHandler, "/ws/saga-dashboard")
                .setAllowedOrigins("*") // Configure appropriately for production
                .withSockJS(); // Enable SockJS fallback
    }
} 