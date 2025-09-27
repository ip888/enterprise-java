package com.enterprise.portfolio.userservice.events;

import java.time.LocalDateTime;

/**
 * Base interface for all user events in the system.
 * This interface implements an event-driven architecture pattern using Java 21 sealed classes.
 */
public sealed interface UserEvent 
    permits UserRegisteredEvent, UserUpdatedEvent, UserDeactivatedEvent, UserEmailVerifiedEvent {
    
    String eventId();
    String eventType();
    Long userId();
    LocalDateTime timestamp();
    String correlationId();
}