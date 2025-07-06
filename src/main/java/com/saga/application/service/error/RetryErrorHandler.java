package com.saga.application.service.error;

import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStep;
import com.saga.domain.model.SagaStatus;
import com.saga.application.service.StepExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Retry error handler in the Chain of Responsibility.
 */
@Slf4j
@Component
public class RetryErrorHandler extends ErrorHandler {
    
    @Override
    public boolean handleError(Saga saga, SagaStep step, StepExecutionResult result) {
        if (step.canRetry()) {
            log.info("Retrying step {} for saga {}, attempt {}", 
                    step.getName(), saga.getSagaId(), step.getRetryCount() + 1);
            
            saga.setStatus(SagaStatus.RETRYING);
            saga.incrementRetryCount();
            step.incrementRetryCount();
            
            return true; // Error handled
        }
        
        return passToNext(saga, step, result);
    }
} 