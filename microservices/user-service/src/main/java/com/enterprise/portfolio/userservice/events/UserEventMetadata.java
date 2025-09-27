package com.enterprise.portfolio.userservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Metadata for user events
 */
public record UserEventMetadata(
    @JsonProperty("source") String source,
    @JsonProperty("version") String version,
    @JsonProperty("description") String description,
    @JsonProperty("correlationId") String correlationId,
    @JsonProperty("createdAt") LocalDateTime createdAt
) {
    /**
     * Factory method to create event metadata
     */
    public static UserEventMetadata create(String description, String source) {
        return new UserEventMetadata(
            source,
            "1.0",
            description,
            null,
            LocalDateTime.now()
        );
    }
}