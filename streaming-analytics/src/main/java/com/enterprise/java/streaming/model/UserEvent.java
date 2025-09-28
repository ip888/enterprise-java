package com.enterprise.java.streaming.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * User event model representing events from the User Service (Project 1).
 * This class mirrors the events produced by the User Service Kafka streams.
 */
public class UserEvent {
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("metadata")
    private Map<String, String> metadata;
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    // Default constructor for Jackson
    public UserEvent() {}
    
    // Constructor
    public UserEvent(String userId, String eventType, LocalDateTime timestamp, 
                    Map<String, String> metadata, String sessionId, String ipAddress) {
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.metadata = metadata;
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String userId;
        private String eventType;
        private LocalDateTime timestamp;
        private Map<String, String> metadata;
        private String sessionId;
        private String ipAddress;
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public UserEvent build() {
            return new UserEvent(userId, eventType, timestamp, metadata, sessionId, ipAddress);
        }
    }
    
    // Getters and setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    @Override
    public String toString() {
        return "UserEvent{" +
                "userId='" + userId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", sessionId='" + sessionId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}