package com.enterprise.java.streaming.flink;

import com.enterprise.java.streaming.model.AlertEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flink serialization schema for AlertEvent objects to Kafka.
 */
public class AlertEventSerializer implements SerializationSchema<AlertEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertEventSerializer.class);
    private final ObjectMapper objectMapper;
    
    public AlertEventSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public byte[] serialize(AlertEvent alert) {
        try {
            String jsonString = objectMapper.writeValueAsString(alert);
            return jsonString.getBytes();
        } catch (Exception e) {
            logger.error("Error serializing AlertEvent: {}", e.getMessage());
            return null;
        }
    }
}

/**
 * Flink serialization schema for analytics results to Kafka.
 */
class AnalyticsSerializer implements SerializationSchema<Tuple2<String, UserActivitySummary>> {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsSerializer.class);
    private final ObjectMapper objectMapper;
    
    public AnalyticsSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public byte[] serialize(Tuple2<String, UserActivitySummary> analytics) {
        try {
            // Create a simplified analytics result for Kafka
            AnalyticsResult result = new AnalyticsResult();
            result.userId = analytics.f0;
            result.summary = analytics.f1;
            result.timestamp = java.time.LocalDateTime.now();
            
            String jsonString = objectMapper.writeValueAsString(result);
            return jsonString.getBytes();
        } catch (Exception e) {
            logger.error("Error serializing analytics result: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Simple wrapper class for analytics results
     */
    static class AnalyticsResult {
        public String userId;
        public UserActivitySummary summary;
        public java.time.LocalDateTime timestamp;
    }
}