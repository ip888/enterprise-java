package com.enterprise.portfolio.userservice.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
public class RealTimeMetricsService {

    // User operation counters
    private final AtomicLong userRegistrations = new AtomicLong(0);
    private final AtomicLong userLogins = new AtomicLong(0);
    private final AtomicLong userUpdates = new AtomicLong(0);
    private final AtomicLong userSearches = new AtomicLong(0);
    
    // Infrastructure operation counters
    private final AtomicLong redisOperations = new AtomicLong(0);
    private final AtomicLong kafkaEvents = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // Recent activity log (thread-safe)
    private final List<ActivityLogEntry> recentActivity = new ArrayList<>();
    private final Object activityLock = new Object();
    private static final int MAX_ACTIVITY_ENTRIES = 100;

    // Activity tracking methods for user operations
    public void recordUserRegistration(String username) {
        userRegistrations.incrementAndGet();
        addActivityEntry("USER_REGISTRATION", "User registered: " + username, "success");
    }

    public void recordUserLogin(String username) {
        userLogins.incrementAndGet();
        addActivityEntry("USER_LOGIN", "User logged in: " + username, "success");
    }

    public void recordUserUpdate(String username) {
        userUpdates.incrementAndGet();
        addActivityEntry("USER_UPDATE", "User profile updated: " + username, "success");
    }

    public void recordUserSearch(String searchTerm, int resultCount) {
        userSearches.incrementAndGet();
        addActivityEntry("USER_SEARCH", "Search performed for '" + searchTerm + "' - " + resultCount + " results", "info");
    }

    // Activity tracking methods for infrastructure operations
    public void recordRedisOperation(String operation, String key) {
        redisOperations.incrementAndGet();
        addActivityEntry("REDIS_OPERATION", operation + " on key: " + key, "info");
    }

    public void recordKafkaEvent(String topic, String event) {
        kafkaEvents.incrementAndGet();
        addActivityEntry("KAFKA_EVENT", "Event published to " + topic + ": " + event, "info");
    }

    public void recordCacheHit(String key) {
        cacheHits.incrementAndGet();
        addActivityEntry("CACHE_HIT", "Cache hit for key: " + key, "success");
    }

    public void recordCacheMiss(String key) {
        cacheMisses.incrementAndGet();
        addActivityEntry("CACHE_MISS", "Cache miss for key: " + key, "warning");
    }

    // Get all current metrics
    public Mono<Map<String, Object>> getCurrentMetrics() {
        return Mono.fromCallable(() -> {
            Map<String, Object> metrics = new ConcurrentHashMap<>();
            
            // User operation metrics
            metrics.put("user_registrations", userRegistrations.get());
            metrics.put("user_logins", userLogins.get());
            metrics.put("user_updates", userUpdates.get());
            metrics.put("user_searches", userSearches.get());
            
            // Infrastructure metrics
            metrics.put("redis_operations", redisOperations.get());
            metrics.put("kafka_events", kafkaEvents.get());
            metrics.put("cache_hits", cacheHits.get());
            metrics.put("cache_misses", cacheMisses.get());
            
            // Calculated metrics
            long totalCacheOperations = cacheHits.get() + cacheMisses.get();
            double cacheHitRatio = totalCacheOperations > 0 ? 
                (double) cacheHits.get() / totalCacheOperations * 100.0 : 0.0;
            metrics.put("cache_hit_ratio", cacheHitRatio);
            
            long totalUserOperations = userRegistrations.get() + userLogins.get() + 
                                     userUpdates.get() + userSearches.get();
            metrics.put("total_user_operations", totalUserOperations);
            
            metrics.put("timestamp", System.currentTimeMillis());
            
            return metrics;
        });
    }

    // Get recent activity log
    public List<ActivityLogEntry> getRecentActivity() {
        synchronized (activityLock) {
            return new ArrayList<>(recentActivity);
        }
    }

    // Reset all metrics
    public void resetAllMetrics() {
        userRegistrations.set(0);
        userLogins.set(0);
        userUpdates.set(0);
        userSearches.set(0);
        redisOperations.set(0);
        kafkaEvents.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        
        synchronized (activityLock) {
            recentActivity.clear();
        }
        
        addActivityEntry("SYSTEM", "All metrics reset", "info");
    }

    // Internal method to add activity entries
    private void addActivityEntry(String type, String message, String level) {
        synchronized (activityLock) {
            ActivityLogEntry entry = new ActivityLogEntry(
                System.currentTimeMillis(),
                LocalDateTime.now(),
                type,
                message,
                level
            );
            
            recentActivity.add(0, entry); // Add to front
            
            // Keep only the most recent entries
            while (recentActivity.size() > MAX_ACTIVITY_ENTRIES) {
                recentActivity.remove(recentActivity.size() - 1);
            }
        }
    }

    // Activity log entry class
    public static class ActivityLogEntry {
        private final long timestamp;
        private final LocalDateTime dateTime;
        private final String type;
        private final String message;
        private final String level;

        public ActivityLogEntry(long timestamp, LocalDateTime dateTime, String type, String message, String level) {
            this.timestamp = timestamp;
            this.dateTime = dateTime;
            this.type = type;
            this.message = message;
            this.level = level;
        }

        // Getters
        public long getTimestamp() { return timestamp; }
        public LocalDateTime getDateTime() { return dateTime; }
        public String getType() { return type; }
        public String getMessage() { return message; }
        public String getLevel() { return level; }
    }
}