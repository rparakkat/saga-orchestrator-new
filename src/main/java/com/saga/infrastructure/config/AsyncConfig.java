package com.saga.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for asynchronous operations and HTTP clients with scalable thread pools.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${saga.execution.thread-pool.core-size:20}")
    private int corePoolSize;

    @Value("${saga.execution.thread-pool.max-size:100}")
    private int maxPoolSize;

    @Value("${saga.execution.thread-pool.queue-capacity:500}")
    private int queueCapacity;

    @Value("${saga.execution.thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    /**
     * Configure async executor for saga operations with scalable thread pool
     */
    @Bean(name = "sagaTaskExecutor")
    public Executor sagaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("saga-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Configure executor for step execution with dedicated thread pool
     */
    @Bean(name = "stepTaskExecutor")
    public Executor stepTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);           // Increased for higher throughput
        executor.setMaxPoolSize(400);            // Increased max threads
        executor.setQueueCapacity(2000);         // Increased queue capacity
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("step-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Configure executor for compensation operations
     */
    @Bean(name = "compensationTaskExecutor")
    public Executor compensationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("compensation-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Configure RestTemplate for HTTP operations with connection pooling
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 