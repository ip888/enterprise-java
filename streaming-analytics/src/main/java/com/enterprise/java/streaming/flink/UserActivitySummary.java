package com.enterprise.java.streaming.flink;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * User Activity Summary model for aggregated analytics
 */
public class UserActivitySummary {
    private String userId;
    private int totalEvents;
    private int uniqueSessions;
    private Map<String, Integer> eventTypeCounts;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private double avgEventsPerSession;
    
    public UserActivitySummary() {
        this.eventTypeCounts = new HashMap<>();
    }
    
    // Constructor
    public UserActivitySummary(String userId) {
        this.userId = userId;
        this.eventTypeCounts = new HashMap<>();
    }
    
    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public int getTotalEvents() { return totalEvents; }
    public void setTotalEvents(int totalEvents) { this.totalEvents = totalEvents; }
    
    public int getUniqueSessions() { return uniqueSessions; }
    public void setUniqueSessions(int uniqueSessions) { this.uniqueSessions = uniqueSessions; }
    
    public Map<String, Integer> getEventTypeCounts() { return eventTypeCounts; }
    public void setEventTypeCounts(Map<String, Integer> eventTypeCounts) { this.eventTypeCounts = eventTypeCounts; }
    
    public LocalDateTime getWindowStart() { return windowStart; }
    public void setWindowStart(LocalDateTime windowStart) { this.windowStart = windowStart; }
    
    public LocalDateTime getWindowEnd() { return windowEnd; }
    public void setWindowEnd(LocalDateTime windowEnd) { this.windowEnd = windowEnd; }
    
    public double getAvgEventsPerSession() { return avgEventsPerSession; }
    public void setAvgEventsPerSession(double avgEventsPerSession) { this.avgEventsPerSession = avgEventsPerSession; }
    
    @Override
    public String toString() {
        return "UserActivitySummary{" +
                "userId='" + userId + '\'' +
                ", totalEvents=" + totalEvents +
                ", uniqueSessions=" + uniqueSessions +
                ", eventTypeCounts=" + eventTypeCounts +
                ", avgEventsPerSession=" + avgEventsPerSession +
                '}';
    }
}