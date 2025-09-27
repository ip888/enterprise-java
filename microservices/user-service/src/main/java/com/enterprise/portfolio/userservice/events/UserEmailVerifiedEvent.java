package com.enterprise.portfolio.userservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a user's email is verified
 */
public record UserEmailVerifiedEvent(
    @JsonProperty("eventId") String eventId,
    @JsonProperty("eventType") String eventType,
    @JsonProperty("userId") Long userId,
    @JsonProperty("email") String email,
    @JsonProperty("verificationMethod") String verificationMethod,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("correlationId") String correlationId,
    @JsonProperty("metadata") UserEventMetadata metadata
) implements UserEvent {
    
    /**
     * Factory method to create a UserEmailVerifiedEvent
     */
    public static UserEmailVerifiedEvent create(Long userId, String email, 
                                              String verificationMethod, String correlationId) {
        return new UserEmailVerifiedEvent(
            UUID.randomUUID().toString(),
            "USER_EMAIL_VERIFIED",
            userId,
            email,
            verificationMethod,
            LocalDateTime.now(),
            correlationId,
            UserEventMetadata.create("Email verification completed", "email-service")
        );
    }
}