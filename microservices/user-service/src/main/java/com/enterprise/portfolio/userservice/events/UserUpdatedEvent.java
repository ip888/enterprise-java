package com.enterprise.portfolio.userservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a user's profile is updated
 */
public record UserUpdatedEvent(
    @JsonProperty("eventId") String eventId,
    @JsonProperty("eventType") String eventType,
    @JsonProperty("userId") Long userId,
    @JsonProperty("updatedFields") String[] updatedFields,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("correlationId") String correlationId,
    @JsonProperty("metadata") UserEventMetadata metadata
) implements UserEvent {
    
    /**
     * Factory method to create a UserUpdatedEvent
     */
    public static UserUpdatedEvent create(Long userId, String[] updatedFields, String correlationId) {
        return new UserUpdatedEvent(
            UUID.randomUUID().toString(),
            "USER_UPDATED",
            userId,
            updatedFields,
            LocalDateTime.now(),
            correlationId,
            UserEventMetadata.create("User profile updated", "user-service")
        );
    }
}