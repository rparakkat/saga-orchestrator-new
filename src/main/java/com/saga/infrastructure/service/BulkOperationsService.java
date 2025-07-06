package com.saga.infrastructure.service;

import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStatus;
import com.saga.infrastructure.repository.SagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Bulk operations service for improved database performance and scalability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BulkOperationsService {

    private final SagaRepository sagaRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * Bulk update saga statuses
     */
    @Async("sagaTaskExecutor")
    public CompletableFuture<Long> bulkUpdateSagaStatuses(
            List<String> sagaIds, SagaStatus newStatus) {
        
        log.info("Bulk updating {} sagas to status: {}", sagaIds.size(), newStatus);
        
        Query query = new Query(Criteria.where("sagaId").in(sagaIds));
        Update update = new Update()
                .set("status", newStatus)
                .set("updatedAt", LocalDateTime.now());
        
        long updatedCount = mongoTemplate.updateMulti(query, update, Saga.class).getModifiedCount();
        log.info("Bulk updated {} sagas", updatedCount);
        
        return CompletableFuture.completedFuture(updatedCount);
    }

    /**
     * Bulk delete completed sagas older than specified date
     */
    @Async("sagaTaskExecutor")
    public CompletableFuture<Long> bulkDeleteOldSagas(LocalDateTime cutoffDate) {
        log.info("Bulk deleting sagas older than: {}", cutoffDate);
        
        Query query = new Query(Criteria.where("createdAt").lt(cutoffDate)
                .and("status").in(SagaStatus.COMPLETED, SagaStatus.FAILED, SagaStatus.COMPENSATED));
        
        long deletedCount = mongoTemplate.remove(query, Saga.class).getDeletedCount();
        log.info("Bulk deleted {} old sagas", deletedCount);
        
        return CompletableFuture.completedFuture(deletedCount);
    }

    /**
     * Bulk retry failed sagas
     */
    @Async("sagaTaskExecutor")
    public CompletableFuture<Long> bulkRetryFailedSagas() {
        log.info("Bulk retrying failed sagas");
        
        Query query = new Query(Criteria.where("status").is(SagaStatus.FAILED)
                .and("retryCount").lt("maxRetries"));
        
        Update update = new Update()
                .set("status", SagaStatus.RUNNING)
                .set("retryCount", 0)
                .set("errorMessage", null)
                .set("errorStackTrace", null)
                .set("completedAt", null)
                .set("updatedAt", LocalDateTime.now());
        
        long updatedCount = mongoTemplate.updateMulti(query, update, Saga.class).getModifiedCount();
        log.info("Bulk retried {} failed sagas", updatedCount);
        
        return CompletableFuture.completedFuture(updatedCount);
    }

    /**
     * Bulk timeout long-running sagas
     */
    @Async("sagaTaskExecutor")
    public CompletableFuture<Long> bulkTimeoutLongRunningSagas(LocalDateTime timeoutThreshold) {
        log.info("Bulk timing out sagas running longer than: {}", timeoutThreshold);
        
        Query query = new Query(Criteria.where("status").is(SagaStatus.RUNNING)
                .and("startedAt").lt(timeoutThreshold));
        
        Update update = new Update()
                .set("status", SagaStatus.TIMEOUT)
                .set("completedAt", LocalDateTime.now())
                .set("updatedAt", LocalDateTime.now());
        
        long updatedCount = mongoTemplate.updateMulti(query, update, Saga.class).getModifiedCount();
        log.info("Bulk timed out {} long-running sagas", updatedCount);
        
        return CompletableFuture.completedFuture(updatedCount);
    }

    /**
     * Bulk insert sagas
     */
    @Async("sagaTaskExecutor")
    public CompletableFuture<Long> bulkInsertSagas(List<Saga> sagas) {
        log.info("Bulk inserting {} sagas", sagas.size());
        
        List<Saga> savedSagas = sagaRepository.saveAll(sagas);
        log.info("Bulk inserted {} sagas", savedSagas.size());
        
        return CompletableFuture.completedFuture((long) savedSagas.size());
    }

    /**
     * Get sagas with pagination for bulk processing
     */
    public Page<Saga> getSagasForBulkProcessing(SagaStatus status, Pageable pageable) {
        return sagaRepository.findByStatus(status, pageable);
    }

    /**
     * Get sagas by date range for bulk processing
     */
    public List<Saga> getSagasByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return sagaRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Get sagas by status and date range for bulk processing
     */
    public List<Saga> getSagasByStatusAndDateRange(
            SagaStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        return sagaRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate);
    }
} 