package com.saga.infrastructure.cache;

import com.saga.domain.model.Saga;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Caching layer for saga data to improve performance and reduce database load.
 * Uses Spring Cache abstraction for flexible cache implementation.
 */
@Slf4j
@Component
public class SagaCache {

    /**
     * Cache saga by ID
     */
    @Cacheable(value = "sagas", key = "#sagaId")
    public Optional<Saga> getSaga(String sagaId) {
        log.debug("Cache miss for saga: {}", sagaId);
        return Optional.empty(); // This will be populated by the repository layer
    }

    /**
     * Cache saga by correlation ID
     */
    @Cacheable(value = "sagas-by-correlation", key = "#correlationId")
    public List<Saga> getSagasByCorrelationId(String correlationId) {
        log.debug("Cache miss for correlation ID: {}", correlationId);
        return List.of(); // This will be populated by the repository layer
    }

    /**
     * Cache sagas by status
     */
    @Cacheable(value = "sagas-by-status", key = "#status.name()")
    public List<Saga> getSagasByStatus(com.saga.domain.model.SagaStatus status) {
        log.debug("Cache miss for status: {}", status);
        return List.of(); // This will be populated by the repository layer
    }

    /**
     * Evict saga from cache when updated
     */
    @CacheEvict(value = {"sagas", "sagas-by-correlation", "sagas-by-status"}, allEntries = true)
    public void evictSaga(String sagaId) {
        log.debug("Evicting saga from cache: {}", sagaId);
    }

    /**
     * Evict all saga caches
     */
    @CacheEvict(value = {"sagas", "sagas-by-correlation", "sagas-by-status"}, allEntries = true)
    public void evictAllSagas() {
        log.debug("Evicting all saga caches");
    }
} 