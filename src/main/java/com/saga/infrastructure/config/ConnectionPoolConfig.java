package com.saga.infrastructure.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Connection pool configuration for MongoDB to improve scalability and performance.
 */
@Slf4j
@Configuration
public class ConnectionPoolConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${saga.mongodb.connection-pool.max-size:100}")
    private int maxConnectionPoolSize;

    @Value("${saga.mongodb.connection-pool.min-size:5}")
    private int minConnectionPoolSize;

    @Value("${saga.mongodb.connection-pool.max-wait-time:30000}")
    private int maxWaitTime;

    @Value("${saga.mongodb.connection-pool.max-connection-life-time:300000}")
    private int maxConnectionLifeTime;

    @Value("${saga.mongodb.connection-pool.max-connection-idle-time:60000}")
    private int maxConnectionIdleTime;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        log.info("Configuring MongoDB connection pool with maxSize: {}, minSize: {}", 
                maxConnectionPoolSize, minConnectionPoolSize);

        ConnectionString connectionString = new ConnectionString(mongoUri);
        
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(maxConnectionPoolSize)
                        .minSize(minConnectionPoolSize)
                        .maxWaitTime(maxWaitTime, TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(maxConnectionLifeTime, TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(maxConnectionIdleTime, TimeUnit.MILLISECONDS))
                .applyToServerSettings(builder -> builder
                        .heartbeatFrequency(10000, TimeUnit.MILLISECONDS)
                        .minHeartbeatFrequency(500, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .readTimeout(30000, TimeUnit.MILLISECONDS))
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
} 