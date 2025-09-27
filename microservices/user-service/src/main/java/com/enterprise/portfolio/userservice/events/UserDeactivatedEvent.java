package com.enterprise.portfolio.userservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a user account is deactivated
 */
public record UserDeactivatedEvent(
    @JsonProperty("eventId") String eventId,
    @JsonProperty("eventType") String eventType,
    @JsonProperty("userId") Long userId,
    @JsonProperty("reason") String reason,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("correlationId") String correlationId,
    @JsonProperty("metadata") UserEventMetadata metadata
) implements UserEvent {
    
    /**
     * Factory method to create a UserDeactivatedEvent
     */
    public static UserDeactivatedEvent create(Long userId, String reason, String correlationId) {
        return new UserDeactivatedEvent(
            UUID.randomUUID().toString(),
            "USER_DEACTIVATED",
            userId,
            reason,
            LocalDateTime.now(),
            correlationId,
            UserEventMetadata.create("User account deactivated", "user-service")
        );
    }
}