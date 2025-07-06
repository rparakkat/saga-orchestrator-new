package com.saga.application.service.error;

import com.saga.domain.model.Saga;
import com.saga.domain.model.SagaStep;
import com.saga.application.service.StepExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Continue error handler for non-required steps in the Chain of Responsibility.
 */
@Slf4j
@Component
public class ContinueErrorHandler extends ErrorHandler {
    
    @Override
    public boolean handleError(Saga saga, SagaStep step, StepExecutionResult result) {
        if (!step.isRequired()) {
            log.warn("Non-required step {} failed for saga {}, continuing with next step", 
                    step.getName(), saga.getSagaId());
            
            saga.moveToNextStep();
            return true; // Error handled
        }
        
        return passToNext(saga, step, result);
    }
} 