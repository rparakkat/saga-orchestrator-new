package com.saga.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting service to protect the API from abuse and ensure fair resource distribution.
 */
@Slf4j
@Service
public class RateLimitingService {

    @Value("${saga.rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    @Value("${saga.rate-limit.requests-per-hour:1000}")
    private int requestsPerHour;

    @Value("${saga.rate-limit.burst-size:50}")
    private int burstSize;

    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    /**
     * Check if request is allowed for the given client
     */
    public boolean isAllowed(String clientId) {
        RateLimitInfo info = rateLimitMap.computeIfAbsent(clientId, k -> new RateLimitInfo());
        
        LocalDateTime now = LocalDateTime.now();
        
        // Clean up old entries
        info.cleanupOldEntries(now);
        
        // Check burst limit
        if (info.getCurrentBurstCount() >= burstSize) {
            log.warn("Rate limit exceeded for client: {} (burst limit)", clientId);
            return false;
        }
        
        // Check per-minute limit
        if (info.getMinuteCount() >= requestsPerMinute) {
            log.warn("Rate limit exceeded for client: {} (per-minute limit)", clientId);
            return false;
        }
        
        // Check per-hour limit
        if (info.getHourCount() >= requestsPerHour) {
            log.warn("Rate limit exceeded for client: {} (per-hour limit)", clientId);
            return false;
        }
        
        // Increment counters
        info.incrementCounters(now);
        
        return true;
    }

    /**
     * Get current rate limit status for a client
     */
    public RateLimitStatus getRateLimitStatus(String clientId) {
        RateLimitInfo info = rateLimitMap.get(clientId);
        if (info == null) {
            return new RateLimitStatus(0, 0, 0, requestsPerMinute, requestsPerHour, burstSize);
        }
        
        LocalDateTime now = LocalDateTime.now();
        info.cleanupOldEntries(now);
        
        return new RateLimitStatus(
                info.getMinuteCount(),
                info.getHourCount(),
                info.getCurrentBurstCount(),
                requestsPerMinute,
                requestsPerHour,
                burstSize
        );
    }

    /**
     * Reset rate limit for a client (admin function)
     */
    public void resetRateLimit(String clientId) {
        rateLimitMap.remove(clientId);
        log.info("Rate limit reset for client: {}", clientId);
    }

    /**
     * Rate limit information for a client
     */
    private static class RateLimitInfo {
        private final AtomicInteger minuteCount = new AtomicInteger(0);
        private final AtomicInteger hourCount = new AtomicInteger(0);
        private final AtomicInteger burstCount = new AtomicInteger(0);
        private LocalDateTime lastMinuteReset = LocalDateTime.now();
        private LocalDateTime lastHourReset = LocalDateTime.now();
        private LocalDateTime lastBurstReset = LocalDateTime.now();

        public void incrementCounters(LocalDateTime now) {
            minuteCount.incrementAndGet();
            hourCount.incrementAndGet();
            burstCount.incrementAndGet();
        }

        public void cleanupOldEntries(LocalDateTime now) {
            // Reset minute counter if a minute has passed
            if (now.isAfter(lastMinuteReset.plusMinutes(1))) {
                minuteCount.set(0);
                lastMinuteReset = now;
            }
            
            // Reset hour counter if an hour has passed
            if (now.isAfter(lastHourReset.plusHours(1))) {
                hourCount.set(0);
                lastHourReset = now;
            }
            
            // Reset burst counter if 10 seconds have passed
            if (now.isAfter(lastBurstReset.plusSeconds(10))) {
                burstCount.set(0);
                lastBurstReset = now;
            }
        }

        public int getMinuteCount() {
            return minuteCount.get();
        }

        public int getHourCount() {
            return hourCount.get();
        }

        public int getCurrentBurstCount() {
            return burstCount.get();
        }
    }

    /**
     * Rate limit status response
     */
    public static class RateLimitStatus {
        private final int currentMinuteCount;
        private final int currentHourCount;
        private final int currentBurstCount;
        private final int minuteLimit;
        private final int hourLimit;
        private final int burstLimit;

        public RateLimitStatus(int currentMinuteCount, int currentHourCount, int currentBurstCount,
                             int minuteLimit, int hourLimit, int burstLimit) {
            this.currentMinuteCount = currentMinuteCount;
            this.currentHourCount = currentHourCount;
            this.currentBurstCount = currentBurstCount;
            this.minuteLimit = minuteLimit;
            this.hourLimit = hourLimit;
            this.burstLimit = burstLimit;
        }

        // Getters
        public int getCurrentMinuteCount() { return currentMinuteCount; }
        public int getCurrentHourCount() { return currentHourCount; }
        public int getCurrentBurstCount() { return currentBurstCount; }
        public int getMinuteLimit() { return minuteLimit; }
        public int getHourLimit() { return hourLimit; }
        public int getBurstLimit() { return burstLimit; }
    }
} 