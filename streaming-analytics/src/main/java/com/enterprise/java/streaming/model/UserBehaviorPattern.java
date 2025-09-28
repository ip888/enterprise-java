package com.enterprise.java.streaming.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * User behavior pattern model for ML analysis results.
 */
public class UserBehaviorPattern {
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("patternId")
    private String patternId;
    
    @JsonProperty("patternType")
    private String patternType; // NORMAL, SUSPICIOUS, HIGH_ACTIVITY, etc.
    
    @JsonProperty("confidence")
    private Double confidence; // 0.0 to 1.0
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("features")
    private Map<String, Double> features;
    
    @JsonProperty("userSegment")
    private String userSegment; // From ML clustering
    
    @JsonProperty("riskScore")
    private Double riskScore; // 0.0 to 1.0
    
    // Default constructor for Jackson
    public UserBehaviorPattern() {}
    
    // Constructor
    public UserBehaviorPattern(String userId, String patternId, String patternType,
                              Double confidence, LocalDateTime timestamp, 
                              Map<String, Double> features, String userSegment, Double riskScore) {
        this.userId = userId;
        this.patternId = patternId;
        this.patternType = patternType;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.features = features;
        this.userSegment = userSegment;
        this.riskScore = riskScore;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String userId;
        private String patternId;
        private String patternType;
        private Double confidence;
        private LocalDateTime timestamp;
        private Map<String, Double> features;
        private String userSegment;
        private Double riskScore;
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder patternId(String patternId) {
            this.patternId = patternId;
            return this;
        }
        
        public Builder patternType(String patternType) {
            this.patternType = patternType;
            return this;
        }
        
        public Builder confidence(Double confidence) {
            this.confidence = confidence;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder features(Map<String, Double> features) {
            this.features = features;
            return this;
        }
        
        public Builder userSegment(String userSegment) {
            this.userSegment = userSegment;
            return this;
        }
        
        public Builder riskScore(Double riskScore) {
            this.riskScore = riskScore;
            return this;
        }
        
        public UserBehaviorPattern build() {
            return new UserBehaviorPattern(userId, patternId, patternType, confidence, 
                                         timestamp, features, userSegment, riskScore);
        }
    }
    
    // Getters and setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getPatternId() {
        return patternId;
    }
    
    public void setPatternId(String patternId) {
        this.patternId = patternId;
    }
    
    public String getPatternType() {
        return patternType;
    }
    
    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Double> getFeatures() {
        return features;
    }
    
    public void setFeatures(Map<String, Double> features) {
        this.features = features;
    }
    
    public String getUserSegment() {
        return userSegment;
    }
    
    public void setUserSegment(String userSegment) {
        this.userSegment = userSegment;
    }
    
    public Double getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }
    
    @Override
    public String toString() {
        return "UserBehaviorPattern{" +
                "userId='" + userId + '\'' +
                ", patternId='" + patternId + '\'' +
                ", patternType='" + patternType + '\'' +
                ", confidence=" + confidence +
                ", timestamp=" + timestamp +
                ", userSegment='" + userSegment + '\'' +
                ", riskScore=" + riskScore +
                '}';
    }
}