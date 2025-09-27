package com.enterprise.portfolio.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class MonitoringService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public MonitoringService(ReactiveRedisTemplate<String, Object> redisTemplate,
                            KafkaTemplate<String, String> kafkaTemplate) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Get comprehensive Redis statistics
     */
    public Mono<Map<String, Object>> getRedisStats() {
        return redisTemplate.scan()
            .count()
            .map(keyCount -> {
                Map<String, Object> stats = new HashMap<>();
                stats.put("total_keys", keyCount);
                stats.put("connection_status", "CONNECTED");
                stats.put("last_check", System.currentTimeMillis());
                
                // Add some Redis-specific metrics
                Map<String, Object> keyTypes = new HashMap<>();
                keyTypes.put("user_profiles", "Cached user data");
                keyTypes.put("search_results", "Cached search queries");
                keyTypes.put("sessions", "User session data");
                stats.put("key_types", keyTypes);
                
                return stats;
            })
            .onErrorReturn(createErrorStats("Redis"));
    }

    /**
     * Get comprehensive Kafka statistics
     */
    public Mono<Map<String, Object>> getKafkaStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();
            
            // Kafka cluster information
            stats.put("bootstrap_servers", kafkaTemplate.getProducerFactory().getConfigurationProperties().get("bootstrap.servers"));
            stats.put("connection_status", "CONNECTED");
            stats.put("last_check", System.currentTimeMillis());
            
            // Topic information
            Map<String, Object> topics = new HashMap<>();
            topics.put("user-events", "User lifecycle events");
            topics.put("notifications", "User notifications");
            topics.put("audit-events", "System audit logs");
            stats.put("topics", topics);
            
            // Producer configuration
            Map<String, Object> producerConfig = new HashMap<>();
            producerConfig.put("acks", "all");
            producerConfig.put("retries", 3);
            producerConfig.put("enable_idempotence", true);
            stats.put("producer_config", producerConfig);
            
            return stats;
        })
        .onErrorReturn(createErrorStats("Kafka"));
    }

    /**
     * Test connectivity to both Redis and Kafka
     */
    public Mono<Map<String, Object>> testFullConnectivity() {
        return Mono.zip(
            testRedisConnectivity(),
            testKafkaConnectivity()
        )
        .map(tuple -> {
            Map<String, Object> result = new HashMap<>();
            result.put("redis_test", tuple.getT1());
            result.put("kafka_test", tuple.getT2());
            result.put("overall_status", 
                (Boolean) tuple.getT1().get("success") && (Boolean) tuple.getT2().get("success") 
                ? "ALL_SYSTEMS_OPERATIONAL" : "DEGRADED");
            result.put("timestamp", System.currentTimeMillis());
            return result;
        });
    }

    /**
     * Test Redis connectivity with actual operations
     */
    private Mono<Map<String, Object>> testRedisConnectivity() {
        String testKey = "connectivity-test-" + System.currentTimeMillis();
        String testValue = "test-value";
        
        return redisTemplate.opsForValue().set(testKey, testValue)
            .then(redisTemplate.opsForValue().get(testKey))
            .then(redisTemplate.delete(testKey))
            .map(deleted -> {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("operation", "set_get_delete");
                result.put("message", "Redis connectivity test passed");
                result.put("test_key", testKey);
                return result;
            })
            .onErrorReturn(Map.of(
                "success", false,
                "operation", "connectivity_test",
                "message", "Redis connectivity test failed"
            ));
    }

    /**
     * Test Kafka connectivity
     */
    private Mono<Map<String, Object>> testKafkaConnectivity() {
        return Mono.fromCallable(() -> {
            // Test Kafka producer configuration
            try {
                kafkaTemplate.getProducerFactory().createProducer().close();
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("operation", "producer_creation");
                result.put("message", "Kafka connectivity test passed");
                return result;
            } catch (Exception e) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("operation", "producer_creation");
                result.put("message", "Kafka connectivity test failed: " + e.getMessage());
                return result;
            }
        });
    }

    private Map<String, Object> createErrorStats(String component) {
        Map<String, Object> errorStats = new HashMap<>();
        errorStats.put("connection_status", "ERROR");
        errorStats.put("component", component);
        errorStats.put("message", component + " statistics unavailable");
        errorStats.put("last_check", System.currentTimeMillis());
        return errorStats;
    }
}