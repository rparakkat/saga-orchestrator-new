package com.saga.infrastructure.controller;

import com.saga.application.service.SagaOrchestratorService;
import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStatus;
import com.saga.domain.model.SagaStep;
import com.saga.infrastructure.dto.CreateSagaRequest;
import com.saga.infrastructure.dto.SagaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Saga orchestration operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sagas")
@RequiredArgsConstructor
public class SagaController {

    private final SagaOrchestratorService sagaOrchestratorService;

    /**
     * Create and start a new saga
     */
    @PostMapping
    public ResponseEntity<SagaResponse> createSaga(@RequestBody CreateSagaRequest request) {
        log.info("Creating saga: {}", request.getName());
        
        try {
            Saga saga = sagaOrchestratorService.createAndStartSaga(
                    request.getName(),
                    request.getSteps(),
                    request.getInputData()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SagaResponse.fromSaga(saga));
                    
        } catch (Exception e) {
            log.error("Error creating saga: {}", request.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SagaResponse.builder()
                            .errorMessage("Failed to create saga: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get saga by ID
     */
    @GetMapping("/{sagaId}")
    public ResponseEntity<SagaResponse> getSaga(@PathVariable String sagaId) {
        log.info("Getting saga: {}", sagaId);
        
        try {
            Saga saga = sagaOrchestratorService.getSaga(sagaId);
            return ResponseEntity.ok(SagaResponse.fromSaga(saga));
            
        } catch (IllegalArgumentException e) {
            log.warn("Saga not found: {}", sagaId);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error getting saga: {}", sagaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SagaResponse.builder()
                            .errorMessage("Failed to get saga: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get sagas by status
     */
    @GetMapping
    public ResponseEntity<List<SagaResponse>> getSagasByStatus(
            @RequestParam(required = false) SagaStatus status) {
        
        log.info("Getting sagas with status: {}", status);
        
        try {
            List<Saga> sagas;
            if (status != null) {
                sagas = sagaOrchestratorService.getSagasByStatus(status);
            } else {
                // TODO: Implement pagination and filtering
                sagas = List.of();
            }
            
            List<SagaResponse> responses = sagas.stream()
                    .map(SagaResponse::fromSaga)
                    .toList();
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error getting sagas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get sagas by correlation ID
     */
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<SagaResponse>> getSagasByCorrelationId(
            @PathVariable String correlationId) {
        
        log.info("Getting sagas for correlation ID: {}", correlationId);
        
        try {
            List<Saga> sagas = sagaOrchestratorService.getSagasByCorrelationId(correlationId);
            List<SagaResponse> responses = sagas.stream()
                    .map(SagaResponse::fromSaga)
                    .toList();
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error getting sagas by correlation ID: {}", correlationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retry a failed saga
     */
    @PostMapping("/{sagaId}/retry")
    public ResponseEntity<SagaResponse> retrySaga(@PathVariable String sagaId) {
        log.info("Retrying saga: {}", sagaId);
        
        try {
            Saga saga = sagaOrchestratorService.retrySaga(sagaId);
            return ResponseEntity.ok(SagaResponse.fromSaga(saga));
            
        } catch (IllegalArgumentException e) {
            log.warn("Saga not found: {}", sagaId);
            return ResponseEntity.notFound().build();
            
        } catch (IllegalStateException e) {
            log.warn("Saga cannot be retried: {}", sagaId);
            return ResponseEntity.badRequest()
                    .body(SagaResponse.builder()
                            .errorMessage("Saga cannot be retried: " + e.getMessage())
                            .build());
            
        } catch (Exception e) {
            log.error("Error retrying saga: {}", sagaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SagaResponse.builder()
                            .errorMessage("Failed to retry saga: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Compensate a saga
     */
    @PostMapping("/{sagaId}/compensate")
    public ResponseEntity<SagaResponse> compensateSaga(@PathVariable String sagaId) {
        log.info("Compensating saga: {}", sagaId);
        
        try {
            Saga saga = sagaOrchestratorService.compensateSaga(sagaId);
            return ResponseEntity.ok(SagaResponse.fromSaga(saga));
            
        } catch (IllegalArgumentException e) {
            log.warn("Saga not found: {}", sagaId);
            return ResponseEntity.notFound().build();
            
        } catch (IllegalStateException e) {
            log.warn("Saga cannot be compensated: {}", sagaId);
            return ResponseEntity.badRequest()
                    .body(SagaResponse.builder()
                            .errorMessage("Saga cannot be compensated: " + e.getMessage())
                            .build());
            
        } catch (Exception e) {
            log.error("Error compensating saga: {}", sagaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SagaResponse.builder()
                            .errorMessage("Failed to compensate saga: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Execute a saga synchronously
     */
    @PostMapping("/{sagaId}/execute")
    public ResponseEntity<SagaResponse> executeSaga(@PathVariable String sagaId) {
        log.info("Executing saga synchronously: {}", sagaId);
        
        try {
            Saga saga = sagaOrchestratorService.executeSaga(sagaId);
            return ResponseEntity.ok(SagaResponse.fromSaga(saga));
            
        } catch (IllegalArgumentException e) {
            log.warn("Saga not found: {}", sagaId);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error executing saga: {}", sagaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SagaResponse.builder()
                            .errorMessage("Failed to execute saga: " + e.getMessage())
                            .build());
        }
    }
} 