package com.saga.infrastructure.repository;

import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Saga entities.
 */
@Repository
public interface SagaRepository extends MongoRepository<Saga, String> {

    /**
     * Find saga by saga ID
     */
    Optional<Saga> findBySagaId(String sagaId);

    /**
     * Find sagas by status
     */
    List<Saga> findByStatus(SagaStatus status);

    /**
     * Find sagas by status with pagination
     */
    Page<Saga> findByStatus(SagaStatus status, Pageable pageable);

    /**
     * Find sagas by name
     */
    List<Saga> findByName(String name);

    /**
     * Find sagas by correlation ID
     */
    List<Saga> findByCorrelationId(String correlationId);

    /**
     * Find sagas by tags
     */
    @Query("{'tags': {$in: ?0}}")
    List<Saga> findByTags(List<String> tags);

    /**
     * Find sagas created between dates
     */
    List<Saga> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find sagas by status and created date range
     */
    List<Saga> findByStatusAndCreatedAtBetween(SagaStatus status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find running sagas that have timed out
     */
    @Query("{'status': 'RUNNING', 'startedAt': {$lt: ?0}, 'timeoutMs': {$gt: 0}}")
    List<Saga> findTimedOutSagas(LocalDateTime timeoutThreshold);

    /**
     * Find failed sagas that can be retried
     */
    @Query("{'status': 'FAILED', 'retryCount': {$lt: '$maxRetries'}}")
    List<Saga> findRetryableSagas();

    /**
     * Find sagas by priority range
     */
    List<Saga> findByPriorityBetween(int minPriority, int maxPriority);

    /**
     * Count sagas by status
     */
    long countByStatus(SagaStatus status);

    /**
     * Delete sagas older than specified date
     */
    void deleteByCreatedAtBefore(LocalDateTime date);

    /**
     * Find sagas by multiple statuses
     */
    List<Saga> findByStatusIn(List<SagaStatus> statuses);

    /**
     * Find sagas by name and status
     */
    List<Saga> findByNameAndStatus(String name, SagaStatus status);

    /**
     * Find sagas by correlation ID and status
     */
    List<Saga> findByCorrelationIdAndStatus(String correlationId, SagaStatus status);
} 