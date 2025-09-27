package com.enterprise.portfolio.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Generic API response wrapper
 */
public record ApiResponse<T>(
    @JsonProperty("success")
    boolean success,

    @JsonProperty("message")
    String message,

    @JsonProperty("data")
    T data,

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,

    @JsonProperty("errors")
    String[] errors
) {
    /**
     * Create success response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, LocalDateTime.now(), null);
    }

    /**
     * Create success response with custom message and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now(), null);
    }

    /**
     * Create success response with message only
     */
    public static <Void> ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now(), null);
    }

    /**
     * Create error response with message
     */
    public static <Void> ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), null);
    }

    /**
     * Create error response with message and errors
     */
    public static <Void> ApiResponse<Void> error(String message, String[] errors) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), errors);
    }
}