package com.enterprise.portfolio.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.enterprise.portfolio.userservice.events.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaEventService {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    // Kafka Topics
    public static final String USER_EVENTS_TOPIC = "user-events";
    public static final String NOTIFICATION_TOPIC = "notifications";
    public static final String AUDIT_TOPIC = "audit-events";
    
    @Autowired
    public KafkaEventService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish user event to Kafka
     */
    public Mono<SendResult<String, String>> publishUserEvent(UserEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.userId().toString();
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(USER_EVENTS_TOPIC, key, eventJson);
            
            return Mono.fromFuture(future);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to serialize user event", e));
        }
    }
    
    /**
     * Publish notification event
     */
    public Mono<SendResult<String, String>> publishNotificationEvent(String userId, String message, String type) {
        try {
            NotificationEvent notification = new NotificationEvent(userId, message, type, System.currentTimeMillis());
            String eventJson = objectMapper.writeValueAsString(notification);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(NOTIFICATION_TOPIC, userId, eventJson);
            
            return Mono.fromFuture(future);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to publish notification", e));
        }
    }
    
    /**
     * Publish audit event
     */
    public Mono<SendResult<String, String>> publishAuditEvent(String action, String userId, String details) {
        try {
            AuditEvent audit = new AuditEvent(action, userId, details, System.currentTimeMillis());
            String eventJson = objectMapper.writeValueAsString(audit);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(AUDIT_TOPIC, userId, eventJson);
            
            return Mono.fromFuture(future);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to publish audit event", e));
        }
    }
    
    /**
     * Test Kafka connectivity by sending a test message
     */
    public Mono<String> testKafkaConnection() {
        try {
            TestEvent testEvent = new TestEvent("test-connection", System.currentTimeMillis());
            String eventJson = objectMapper.writeValueAsString(testEvent);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send("test-topic", "test-key", eventJson);
            
            return Mono.fromFuture(future)
                .map(result -> "Kafka connection successful - Message sent to partition " + 
                              result.getRecordMetadata().partition())
                .onErrorReturn("Kafka connection failed");
        } catch (Exception e) {
            return Mono.just("Kafka connection error: " + e.getMessage());
        }
    }
    
    // Event DTOs
    public static class NotificationEvent {
        private String userId;
        private String message;
        private String type;
        private long timestamp;
        
        public NotificationEvent() {}
        
        public NotificationEvent(String userId, String message, String type, long timestamp) {
            this.userId = userId;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    public static class AuditEvent {
        private String action;
        private String userId;
        private String details;
        private long timestamp;
        
        public AuditEvent() {}
        
        public AuditEvent(String action, String userId, String details, long timestamp) {
            this.action = action;
            this.userId = userId;
            this.details = details;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    public static class TestEvent {
        private String message;
        private long timestamp;
        
        public TestEvent() {}
        
        public TestEvent(String message, long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}