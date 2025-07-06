package com.saga.application.service.command;

import com.saga.domain.model.Saga;
import com.saga.application.service.execution.SagaExecutionTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command for executing sagas using the Command pattern.
 */
@Slf4j
@RequiredArgsConstructor
public class ExecuteSagaCommand implements SagaCommand {
    
    private final SagaExecutionTemplate executionTemplate;
    private final Saga saga;
    
    @Override
    public Saga execute() {
        log.info("Executing saga command for saga: {}", saga.getSagaId());
        return executionTemplate.execute(saga);
    }
    
    @Override
    public Saga undo() {
        log.info("Undoing saga execution for saga: {}", saga.getSagaId());
        // In a real implementation, this would trigger compensation
        // For now, we'll just return the original saga
        return saga;
    }
    
    @Override
    public String getDescription() {
        return "Execute saga: " + saga.getSagaId();
    }
} 