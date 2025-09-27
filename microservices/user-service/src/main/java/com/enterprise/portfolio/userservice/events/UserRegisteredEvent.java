package com.enterprise.portfolio.userservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new user registers
 */
public record UserRegisteredEvent(
    @JsonProperty("eventId") String eventId,
    @JsonProperty("eventType") String eventType,
    @JsonProperty("userId") Long userId,
    @JsonProperty("username") String username,
    @JsonProperty("email") String email,
    @JsonProperty("firstName") String firstName,
    @JsonProperty("lastName") String lastName,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("correlationId") String correlationId,
    @JsonProperty("metadata") UserEventMetadata metadata
) implements UserEvent {
    
    /**
     * Factory method to create a UserRegisteredEvent
     */
    public static UserRegisteredEvent create(Long userId, String username, String email, 
                                           String firstName, String lastName, String correlationId) {
        return new UserRegisteredEvent(
            UUID.randomUUID().toString(),
            "USER_REGISTERED",
            userId,
            username,
            email,
            firstName,
            lastName,
            LocalDateTime.now(),
            correlationId,
            UserEventMetadata.create("Registration completed", "registration-service")
        );
    }
}