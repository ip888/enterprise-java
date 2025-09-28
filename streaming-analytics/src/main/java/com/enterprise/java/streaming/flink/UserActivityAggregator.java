package com.enterprise.java.streaming.flink;

import com.enterprise.java.streaming.model.UserEvent;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Flink AggregateFunction for user activity analysis
 */
public class UserActivityAggregator implements AggregateFunction<UserEvent, UserActivityAccumulator, Tuple2<String, UserActivitySummary>> {
    
    @Override
    public UserActivityAccumulator createAccumulator() {
        return new UserActivityAccumulator();
    }
    
    @Override
    public UserActivityAccumulator add(UserEvent event, UserActivityAccumulator accumulator) {
        accumulator.userId = event.getUserId();
        accumulator.totalEvents++;
        
        // Track unique sessions
        if (!accumulator.sessionIds.contains(event.getSessionId())) {
            accumulator.sessionIds.add(event.getSessionId());
            accumulator.uniqueSessions++;
        }
        
        // Count event types
        accumulator.eventTypeCounts.merge(event.getEventType(), 1, Integer::sum);
        
        // Track time window
        if (accumulator.firstEventTime == null || event.getTimestamp().isBefore(accumulator.firstEventTime)) {
            accumulator.firstEventTime = event.getTimestamp();
        }
        if (accumulator.lastEventTime == null || event.getTimestamp().isAfter(accumulator.lastEventTime)) {
            accumulator.lastEventTime = event.getTimestamp();
        }
        
        return accumulator;
    }
    
    @Override
    public Tuple2<String, UserActivitySummary> getResult(UserActivityAccumulator accumulator) {
        UserActivitySummary summary = new UserActivitySummary(accumulator.userId);
        summary.setTotalEvents(accumulator.totalEvents);
        summary.setUniqueSessions(accumulator.uniqueSessions);
        summary.setEventTypeCounts(accumulator.eventTypeCounts);
        summary.setWindowStart(accumulator.firstEventTime);
        summary.setWindowEnd(accumulator.lastEventTime);
        
        if (accumulator.uniqueSessions > 0) {
            summary.setAvgEventsPerSession((double) accumulator.totalEvents / accumulator.uniqueSessions);
        }
        
        return new Tuple2<>(accumulator.userId, summary);
    }
    
    @Override
    public UserActivityAccumulator merge(UserActivityAccumulator a, UserActivityAccumulator b) {
        UserActivityAccumulator merged = new UserActivityAccumulator();
        merged.userId = a.userId != null ? a.userId : b.userId;
        merged.totalEvents = a.totalEvents + b.totalEvents;
        merged.uniqueSessions = a.uniqueSessions + b.uniqueSessions;
        
        // Merge event type counts
        merged.eventTypeCounts.putAll(a.eventTypeCounts);
        b.eventTypeCounts.forEach((key, value) -> 
            merged.eventTypeCounts.merge(key, value, Integer::sum));
        
        // Merge session IDs
        merged.sessionIds.addAll(a.sessionIds);
        merged.sessionIds.addAll(b.sessionIds);
        merged.uniqueSessions = merged.sessionIds.size();
        
        // Merge time windows
        if (a.firstEventTime != null && b.firstEventTime != null) {
            merged.firstEventTime = a.firstEventTime.isBefore(b.firstEventTime) ? 
                a.firstEventTime : b.firstEventTime;
        } else {
            merged.firstEventTime = a.firstEventTime != null ? a.firstEventTime : b.firstEventTime;
        }
        
        if (a.lastEventTime != null && b.lastEventTime != null) {
            merged.lastEventTime = a.lastEventTime.isAfter(b.lastEventTime) ? 
                a.lastEventTime : b.lastEventTime;
        } else {
            merged.lastEventTime = a.lastEventTime != null ? a.lastEventTime : b.lastEventTime;
        }
        
        return merged;
    }
}

/**
 * Accumulator class for user activity aggregation
 */
class UserActivityAccumulator {
    String userId;
    int totalEvents = 0;
    int uniqueSessions = 0;
    Map<String, Integer> eventTypeCounts = new HashMap<>();
    java.util.Set<String> sessionIds = new java.util.HashSet<>();
    LocalDateTime firstEventTime;
    LocalDateTime lastEventTime;
}