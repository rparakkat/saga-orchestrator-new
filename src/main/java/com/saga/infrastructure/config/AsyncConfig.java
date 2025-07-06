package com.saga.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous operations and HTTP clients.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configure async executor for saga operations
     */
    @Bean(name = "sagaTaskExecutor")
    public Executor sagaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("saga-");
        executor.initialize();
        return executor;
    }

    /**
     * Configure RestTemplate for HTTP operations
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 