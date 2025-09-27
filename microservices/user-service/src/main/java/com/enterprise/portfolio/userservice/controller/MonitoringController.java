package com.enterprise.portfolio.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.enterprise.portfolio.userservice.service.CacheService;
import com.enterprise.portfolio.userservice.service.KafkaEventService;
import com.enterprise.portfolio.userservice.service.MonitoringService;
import com.enterprise.portfolio.userservice.service.RealTimeMetricsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name = "Real-time Monitoring", description = "Live monitoring dashboard for Kafka and Redis")
public class MonitoringController {

    private final CacheService cacheService;
    private final KafkaEventService kafkaEventService;
    private final MonitoringService monitoringService;
    private final RealTimeMetricsService realTimeMetricsService;
    
    // Legacy counters for compatibility (will be replaced by RealTimeMetricsService)
    private final AtomicLong redisOperations = new AtomicLong(0);
    private final AtomicLong kafkaEvents = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    @Autowired
    public MonitoringController(CacheService cacheService, 
                               KafkaEventService kafkaEventService,
                               MonitoringService monitoringService,
                               RealTimeMetricsService realTimeMetricsService) {
        this.cacheService = cacheService;
        this.kafkaEventService = kafkaEventService;
        this.monitoringService = monitoringService;
        this.realTimeMetricsService = realTimeMetricsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get monitoring dashboard data", description = "Returns comprehensive monitoring data for Kafka and Redis")
    public Mono<Map<String, Object>> getDashboardData() {
        return Mono.zip(
            cacheService.ping().onErrorReturn("Redis DOWN"),
            kafkaEventService.testKafkaConnection().onErrorReturn("Kafka DOWN"),
            realTimeMetricsService.getCurrentMetrics(),
            monitoringService.getRedisStats(),
            monitoringService.getKafkaStats()
        )
        .map(tuple -> {
            Map<String, Object> dashboard = new HashMap<>();
            
            // System Status
            dashboard.put("timestamp", System.currentTimeMillis());
            dashboard.put("redis_status", tuple.getT1().contains("successful") ? "UP" : "DOWN");
            dashboard.put("kafka_status", tuple.getT2().contains("successful") ? "UP" : "DOWN");
            
            // Real-time Metrics from RealTimeMetricsService
            dashboard.put("real_time_metrics", tuple.getT3());
            
            // Legacy metrics for compatibility
            Map<String, Object> legacyMetrics = new HashMap<>();
            legacyMetrics.put("redis_operations", redisOperations.get());
            legacyMetrics.put("kafka_events", kafkaEvents.get());
            legacyMetrics.put("cache_hits", cacheHits.get());
            legacyMetrics.put("cache_misses", cacheMisses.get());
            legacyMetrics.put("cache_hit_ratio", calculateCacheHitRatio());
            dashboard.put("legacy_metrics", legacyMetrics);
            
            // Component Details
            dashboard.put("redis_stats", tuple.getT3());
            dashboard.put("kafka_stats", tuple.getT4());
            
            return dashboard;
        });
    }

    @GetMapping(value = "/live-metrics", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Live metrics stream", description = "Real-time streaming metrics for Kafka and Redis")
    public Flux<Map<String, Object>> getLiveMetrics() {
        return Flux.interval(Duration.ofSeconds(2))
            .flatMap(tick -> {
                // Simulate some activity for demonstration
                if (tick % 3 == 0) redisOperations.incrementAndGet();
                if (tick % 2 == 0) kafkaEvents.incrementAndGet();
                if (tick % 5 == 0) cacheHits.incrementAndGet();
                if (tick % 7 == 0) cacheMisses.incrementAndGet();
                
                return getDashboardData();
            });
    }

    @PostMapping("/simulate/redis-activity")
    @Operation(summary = "Simulate Redis activity", description = "Generate Redis cache operations for monitoring demonstration")
    public Mono<Map<String, Object>> simulateRedisActivity(@RequestParam(defaultValue = "10") int operations) {
        return Flux.range(1, operations)
            .flatMap(i -> {
                String key = "demo-key-" + i;
                String value = "demo-value-" + System.currentTimeMillis();
                
                return cacheService.cacheUserProfile(key, value)
                    .then(cacheService.getCachedUserProfile(key))
                    .doOnNext(result -> {
                        redisOperations.incrementAndGet();
                        if (result != null) {
                            cacheHits.incrementAndGet();
                        } else {
                            cacheMisses.incrementAndGet();
                        }
                    });
            })
            .then(Mono.fromCallable(() -> {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "SUCCESS");
                result.put("operations_executed", operations);
                result.put("total_redis_operations", redisOperations.get());
                result.put("timestamp", System.currentTimeMillis());
                return result;
            }));
    }

    @PostMapping("/simulate/kafka-activity")
    @Operation(summary = "Simulate Kafka activity", description = "Generate Kafka events for monitoring demonstration")
    public Mono<Map<String, Object>> simulateKafkaActivity(@RequestParam(defaultValue = "5") int events) {
        return Flux.range(1, events)
            .flatMap(i -> {
                String userId = "demo-user-" + i;
                String message = "Demo notification " + i + " at " + System.currentTimeMillis();
                
                return kafkaEventService.publishNotificationEvent(userId, message, "demo")
                    .doOnNext(result -> kafkaEvents.incrementAndGet());
            })
            .then(Mono.fromCallable(() -> {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "SUCCESS");
                result.put("events_published", events);
                result.put("total_kafka_events", kafkaEvents.get());
                result.put("timestamp", System.currentTimeMillis());
                return result;
            }));
    }

    @PostMapping("/simulate/full-activity")
    @Operation(summary = "Simulate full system activity", description = "Generate both Redis and Kafka activity for comprehensive monitoring")
    public Mono<Map<String, Object>> simulateFullActivity() {
        Mono<Void> redisActivity = simulateRedisActivity(5).then();
        Mono<Void> kafkaActivity = simulateKafkaActivity(3).then();
        
        return Mono.when(redisActivity, kafkaActivity)
            .then(getDashboardData())
            .map(dashboardData -> {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "FULL_SIMULATION_COMPLETE");
                result.put("dashboard_data", dashboardData);
                result.put("timestamp", System.currentTimeMillis());
                return result;
            });
    }

    @GetMapping("/health-summary")
    @Operation(summary = "System health summary", description = "Quick health status of all monitored components")
    public Mono<Map<String, Object>> getHealthSummary() {
        return Mono.zip(
            cacheService.ping().map(result -> result.contains("successful")),
            kafkaEventService.testKafkaConnection().map(result -> result.contains("successful"))
        )
        .map(tuple -> {
            Map<String, Object> health = new HashMap<>();
            health.put("overall_status", tuple.getT1() && tuple.getT2() ? "HEALTHY" : "DEGRADED");
            health.put("redis_healthy", tuple.getT1());
            health.put("kafka_healthy", tuple.getT2());
            health.put("components_up", (tuple.getT1() ? 1 : 0) + (tuple.getT2() ? 1 : 0));
            health.put("components_total", 2);
            health.put("timestamp", System.currentTimeMillis());
            return health;
        });
    }

    @DeleteMapping("/reset-metrics")
    @Operation(summary = "Reset monitoring metrics", description = "Reset all monitoring counters to zero")
    public Mono<Map<String, Object>> resetMetrics() {
        // Reset both legacy and real-time metrics
        redisOperations.set(0);
        kafkaEvents.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        
        realTimeMetricsService.resetAllMetrics();
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "METRICS_RESET");
        result.put("message", "All monitoring counters reset to zero (legacy and real-time)");
        result.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(result);
    }

    @GetMapping("/activity-log")
    @Operation(summary = "Get recent activity log", description = "Get the most recent user and system activities")
    public Mono<Map<String, Object>> getActivityLog() {
        return Mono.fromCallable(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("recent_activity", realTimeMetricsService.getRecentActivity());
            response.put("timestamp", System.currentTimeMillis());
            response.put("message", "Real-time activity log showing actual user operations and infrastructure events");
            return response;
        });
    }

    private double calculateCacheHitRatio() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        
        if (total == 0) return 0.0;
        return (double) hits / total * 100.0;
    }
}