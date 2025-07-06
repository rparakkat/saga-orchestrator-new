package com.saga.application.service.builder;

import com.saga.domain.model.*;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder pattern for creating complex saga configurations.
 */
@Data
@Builder
public class SagaBuilder {
    
    private String name;
    private String correlationId;
    private Map<String, Object> inputData;
    private List<SagaStep> steps;
    private Integer priority;
    private List<String> tags;
    private Map<String, Object> metadata;
    private Integer maxRetries;
    private Long timeoutMs;
    
    public static class SagaBuilderImpl {
        private String name;
        private String correlationId;
        private Map<String, Object> inputData = new HashMap<>();
        private List<SagaStep> steps = new ArrayList<>();
        private Integer priority = 0;
        private List<String> tags = new ArrayList<>();
        private Map<String, Object> metadata = new HashMap<>();
        private Integer maxRetries = 3;
        private Long timeoutMs = 30000L;
        
        public SagaBuilderImpl name(String name) {
            this.name = name;
            return this;
        }
        
        public SagaBuilderImpl correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public SagaBuilderImpl inputData(Map<String, Object> inputData) {
            this.inputData.putAll(inputData);
            return this;
        }
        
        public SagaBuilderImpl addInputData(String key, Object value) {
            this.inputData.put(key, value);
            return this;
        }
        
        public SagaBuilderImpl steps(List<SagaStep> steps) {
            this.steps.addAll(steps);
            return this;
        }
        
        public SagaBuilderImpl addStep(SagaStep step) {
            this.steps.add(step);
            return this;
        }
        
        public SagaBuilderImpl priority(Integer priority) {
            this.priority = priority;
            return this;
        }
        
        public SagaBuilderImpl tags(List<String> tags) {
            this.tags.addAll(tags);
            return this;
        }
        
        public SagaBuilderImpl addTag(String tag) {
            this.tags.add(tag);
            return this;
        }
        
        public SagaBuilderImpl metadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }
        
        public SagaBuilderImpl addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public SagaBuilderImpl maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public SagaBuilderImpl timeoutMs(Long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }
        
        public SagaBuilder build() {
            return new SagaBuilder(name, correlationId, inputData, steps, priority, tags, metadata, maxRetries, timeoutMs);
        }
    }
    
    public static SagaBuilderImpl builder() {
        return new SagaBuilderImpl();
    }
} 