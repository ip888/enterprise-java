package com.enterprise.portfolio.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Error response DTO for API errors
 */
public record ErrorResponse(
    @JsonProperty("error")
    String error,

    @JsonProperty("message")
    String message,

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,

    @JsonProperty("path")
    String path,

    @JsonProperty("status")
    int status,

    @JsonProperty("details")
    String[] details
) {
    /**
     * Create error response
     */
    public static ErrorResponse of(String error, String message, String path, int status) {
        return new ErrorResponse(error, message, LocalDateTime.now(), path, status, null);
    }

    /**
     * Create error response with details
     */
    public static ErrorResponse of(String error, String message, String path, int status, String[] details) {
        return new ErrorResponse(error, message, LocalDateTime.now(), path, status, details);
    }
}