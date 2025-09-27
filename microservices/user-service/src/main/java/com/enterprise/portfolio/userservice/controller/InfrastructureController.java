package com.enterprise.portfolio.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import com.enterprise.portfolio.userservice.service.CacheService;
import com.enterprise.portfolio.userservice.service.KafkaEventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/infrastructure")
@Tag(name = "Infrastructure", description = "Infrastructure testing and monitoring endpoints")
public class InfrastructureController {

    private final CacheService cacheService;
    private final KafkaEventService kafkaEventService;

    @Autowired
    public InfrastructureController(CacheService cacheService, KafkaEventService kafkaEventService) {
        this.cacheService = cacheService;
        this.kafkaEventService = kafkaEventService;
    }

    @GetMapping("/health/redis")
    @Operation(summary = "Test Redis connectivity", description = "Ping Redis server to test connectivity")
    public Mono<Map<String, Object>> testRedis() {
        return cacheService.ping()
            .zipWith(cacheService.getRedisInfo())
            .map(tuple -> {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "UP");
                result.put("ping", tuple.getT1());
                result.put("version", tuple.getT2());
                result.put("timestamp", System.currentTimeMillis());
                return result;
            })
            .onErrorReturn(createErrorResponse("Redis", "Connection failed"));
    }

    @GetMapping("/health/kafka")
    @Operation(summary = "Test Kafka connectivity", description = "Send test message to Kafka to verify connectivity")
    public Mono<Map<String, Object>> testKafka() {
        return kafkaEventService.testKafkaConnection()
            .map(result -> {
                Map<String, Object> response = new HashMap<>();
                if (result.contains("successful")) {
                    response.put("status", "UP");
                    response.put("message", result);
                } else {
                    response.put("status", "DOWN");
                    response.put("error", result);
                }
                response.put("timestamp", System.currentTimeMillis());
                return response;
            })
            .onErrorReturn(createErrorResponse("Kafka", "Test message failed"));
    }

    @PostMapping("/test/redis/cache")
    @Operation(summary = "Test Redis caching", description = "Test Redis by setting and getting a cache value")
    public Mono<Map<String, Object>> testRedisCache(@RequestBody Map<String, String> testData) {
        String key = testData.getOrDefault("key", "test-key");
        String value = testData.getOrDefault("value", "test-value");
        
        return cacheService.cacheUserProfile(key, value)
            .then(cacheService.getCachedUserProfile(key))
            .map(cachedValue -> {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "SUCCESS");
                result.put("operation", "cache_test");
                result.put("key", key);
                result.put("original_value", value);
                result.put("cached_value", cachedValue);
                result.put("match", value.equals(cachedValue.toString()));
                result.put("timestamp", System.currentTimeMillis());
                return result;
            })
            .onErrorReturn(createErrorResponse("Redis Cache", "Cache test failed"));
    }

    @PostMapping("/test/kafka/event")
    @Operation(summary = "Test Kafka event publishing", description = "Publish a test event to Kafka")
    public Mono<Map<String, Object>> testKafkaEvent(@RequestBody Map<String, String> eventData) {
        String userId = eventData.getOrDefault("userId", "test-user");
        String message = eventData.getOrDefault("message", "test-notification");
        String type = eventData.getOrDefault("type", "test");
        
        return kafkaEventService.publishNotificationEvent(userId, message, type)
            .map(result -> {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "SUCCESS");
                response.put("operation", "kafka_event_test");
                response.put("topic", result.getRecordMetadata().topic());
                response.put("partition", result.getRecordMetadata().partition());
                response.put("offset", result.getRecordMetadata().offset());
                response.put("timestamp", System.currentTimeMillis());
                response.put("event_data", Map.of(
                    "userId", userId,
                    "message", message,
                    "type", type
                ));
                return response;
            })
            .onErrorReturn(createErrorResponse("Kafka Event", "Event publishing failed"));
    }

    @PostMapping("/test/integration")
    @Operation(summary = "Test Redis + Kafka integration", description = "Test both Redis caching and Kafka event publishing together")
    public Mono<Map<String, Object>> testIntegration(@RequestBody Map<String, String> testData) {
        String userId = testData.getOrDefault("userId", "integration-test-user");
        String sessionData = testData.getOrDefault("sessionData", "test-session-data");
        
        // Test Redis caching
        Mono<String> redisMono = cacheService.cacheUserProfile(userId, sessionData)
            .then(cacheService.getCachedUserProfile(userId))
            .map(Object::toString);
            
        // Test Kafka event
        Mono<String> kafkaMono = kafkaEventService.publishNotificationEvent(
            userId, "Integration test completed", "integration_test")
            .map(result -> "Message sent to partition " + result.getRecordMetadata().partition());
        
        return Mono.zip(redisMono, kafkaMono)
            .map(tuple -> {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "SUCCESS");
                result.put("operation", "integration_test");
                result.put("redis_result", tuple.getT1());
                result.put("kafka_result", tuple.getT2());
                result.put("timestamp", System.currentTimeMillis());
                return result;
            })
            .onErrorReturn(createErrorResponse("Integration", "Integration test failed"));
    }

    @DeleteMapping("/test/redis/clear")
    @Operation(summary = "Clear Redis test caches", description = "Clear all user-related caches for testing")
    public Mono<Map<String, Object>> clearTestCaches() {
        return cacheService.clearUserCaches()
            .map(count -> {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "SUCCESS");
                result.put("operation", "clear_caches");
                result.put("cleared_keys", count);
                result.put("timestamp", System.currentTimeMillis());
                return result;
            })
            .onErrorReturn(createErrorResponse("Clear Cache", "Cache clearing failed"));
    }

    private Map<String, Object> createErrorResponse(String component, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", "ERROR");
        error.put("component", component);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}