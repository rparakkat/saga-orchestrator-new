package com.saga.application.service;

import com.saga.domain.model.*;
import com.saga.domain.exception.SagaExecutionException;
import com.saga.infrastructure.repository.SagaRepository;
import com.saga.application.service.execution.SagaExecutionTemplate;
import com.saga.application.service.command.SagaCommand;
import com.saga.application.service.command.ExecuteSagaCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test for the improved Saga Orchestrator Service.
 */
@ExtendWith(MockitoExtension.class)
class SagaOrchestratorServiceTest {

    @Mock
    private SagaRepository sagaRepository;

    @Mock
    private SagaExecutionTemplate executionTemplate;

    @Mock
    private CompensationService compensationService;

    @Mock
    private SagaEventPublisher eventPublisher;

    private SagaOrchestratorService sagaOrchestratorService;

    @BeforeEach
    void setUp() {
        sagaOrchestratorService = new SagaOrchestratorService(
                sagaRepository, executionTemplate, compensationService, eventPublisher);
    }

    @Test
    void testCreateAndStartSaga_Success() {
        // Given
        String name = "test-saga";
        List<SagaStep> steps = createTestSteps();
        Map<String, Object> inputData = Map.of("key", "value");

        Saga savedSaga = createTestSaga();
        when(sagaRepository.save(any(Saga.class))).thenReturn(savedSaga);

        // When
        Saga result = sagaOrchestratorService.createAndStartSaga(name, steps, inputData);

        // Then
        assertNotNull(result);
        assertEquals("saga-test-id", result.getSagaId());
        assertEquals(name, result.getName());
        assertEquals(SagaStatus.CREATED, result.getStatus());
        verify(sagaRepository).save(any(Saga.class));
    }

    @Test
    void testExecuteSaga_Success() {
        // Given
        String sagaId = "saga-test-id";
        Saga saga = createTestSaga();
        saga.setStatus(SagaStatus.CREATED);

        when(sagaRepository.findBySagaId(sagaId)).thenReturn(Optional.of(saga));
        when(executionTemplate.execute(any(Saga.class))).thenReturn(saga);
        when(sagaRepository.save(any(Saga.class))).thenReturn(saga);

        // When
        Saga result = sagaOrchestratorService.executeSaga(sagaId);

        // Then
        assertNotNull(result);
        verify(executionTemplate).execute(saga);
        verify(sagaRepository).save(saga);
    }

    @Test
    void testExecuteSaga_SagaNotFound() {
        // Given
        String sagaId = "non-existent-saga";
        when(sagaRepository.findBySagaId(sagaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(SagaExecutionException.class, () -> {
            sagaOrchestratorService.executeSaga(sagaId);
        });
    }

    @Test
    void testExecuteSaga_AlreadyCompleted() {
        // Given
        String sagaId = "saga-test-id";
        Saga saga = createTestSaga();
        saga.setStatus(SagaStatus.COMPLETED);

        when(sagaRepository.findBySagaId(sagaId)).thenReturn(Optional.of(saga));

        // When
        Saga result = sagaOrchestratorService.executeSaga(sagaId);

        // Then
        assertEquals(saga, result);
        verify(executionTemplate, never()).execute(any(Saga.class));
    }

    @Test
    void testRetrySaga_Success() {
        // Given
        String sagaId = "saga-test-id";
        Saga saga = createTestSaga();
        saga.setStatus(SagaStatus.FAILED);
        saga.setRetryCount(1);
        saga.setMaxRetries(3);

        when(sagaRepository.findBySagaId(sagaId)).thenReturn(Optional.of(saga));
        when(sagaRepository.save(any(Saga.class))).thenReturn(saga);

        // When
        Saga result = sagaOrchestratorService.retrySaga(sagaId);

        // Then
        assertNotNull(result);
        assertEquals(SagaStatus.RUNNING, result.getStatus());
        assertEquals(0, result.getRetryCount());
        assertNull(result.getErrorMessage());
        verify(sagaRepository).save(saga);
    }

    @Test
    void testRetrySaga_CannotRetry() {
        // Given
        String sagaId = "saga-test-id";
        Saga saga = createTestSaga();
        saga.setStatus(SagaStatus.COMPLETED);

        when(sagaRepository.findBySagaId(sagaId)).thenReturn(Optional.of(saga));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            sagaOrchestratorService.retrySaga(sagaId);
        });
    }

    @Test
    void testCompensateSaga_Success() {
        // Given
        String sagaId = "saga-test-id";
        Saga saga = createTestSaga();
        saga.setStatus(SagaStatus.FAILED);

        when(sagaRepository.findBySagaId(sagaId)).thenReturn(Optional.of(saga));
        when(compensationService.compensateSaga(any(Saga.class))).thenReturn(saga);

        // When
        Saga result = sagaOrchestratorService.compensateSaga(sagaId);

        // Then
        assertNotNull(result);
        verify(compensationService).compensateSaga(saga);
    }

    @Test
    void testGetSaga_Success() {
        // Given
        String sagaId = "saga-test-id";
        Saga saga = createTestSaga();
        when(sagaRepository.findBySagaId(sagaId)).thenReturn(Optional.of(saga));

        // When
        Saga result = sagaOrchestratorService.getSaga(sagaId);

        // Then
        assertEquals(saga, result);
    }

    @Test
    void testGetSaga_NotFound() {
        // Given
        String sagaId = "non-existent-saga";
        when(sagaRepository.findBySagaId(sagaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            sagaOrchestratorService.getSaga(sagaId);
        });
    }

    // Helper methods

    private Saga createTestSaga() {
        return Saga.builder()
                .sagaId("saga-test-id")
                .name("test-saga")
                .status(SagaStatus.CREATED)
                .steps(createTestSteps())
                .currentStepIndex(0)
                .inputData(Map.of("key", "value"))
                .outputData(Map.of())
                .retryCount(0)
                .maxRetries(3)
                .timeoutMs(30000L)
                .priority(0)
                .build();
    }

    private List<SagaStep> createTestSteps() {
        SagaStep step1 = SagaStep.builder()
                .stepId("step-1")
                .name("Test Step 1")
                .order(0)
                .status(StepStatus.CREATED)
                .type(StepType.HTTP_CALL)
                .config(StepConfig.builder()
                        .url("http://test-service/api/test")
                        .httpMethod("POST")
                        .timeoutMs(5000)
                        .maxRetries(3)
                        .build())
                .required(true)
                .compensatable(true)
                .build();

        return List.of(step1);
    }
} 