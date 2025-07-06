package com.saga.application.service.error;

import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStep;
import com.saga.domain.model.SagaStatus;
import com.saga.application.service.StepExecutionResult;
import com.saga.application.service.CompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Compensation error handler in the Chain of Responsibility.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationErrorHandler extends ErrorHandler {
    
    private final CompensationService compensationService;
    
    @Override
    public boolean handleError(Saga saga, SagaStep step, StepExecutionResult result) {
        if (step.isRequired()) {
            log.info("Starting compensation for saga {} due to failed required step {}", 
                    saga.getSagaId(), step.getName());
            
            saga.setStatus(SagaStatus.COMPENSATING);
            compensationService.compensateSaga(saga);
            
            return true; // Error handled
        }
        
        return passToNext(saga, step, result);
    }
} 