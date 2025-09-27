package com.enterprise.portfolio.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.enterprise.portfolio.userservice.domain.User;
import java.time.LocalDateTime;

/**
 * Response DTO for user data
 */
public record UserResponse(
    @JsonProperty("id")
    Long id,

    @JsonProperty("username")
    String username,

    @JsonProperty("email")
    String email,

    @JsonProperty("firstName")
    String firstName,

    @JsonProperty("lastName")
    String lastName,

    @JsonProperty("emailVerified")
    Boolean emailVerified,

    @JsonProperty("enabled")
    Boolean enabled,

    @JsonProperty("roles")
    String[] roles,

    @JsonProperty("lastLoginAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime lastLoginAt,

    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt,

    @JsonProperty("profileCompleteness")
    Integer profileCompleteness,

    @JsonProperty("accountStatus")
    String accountStatus
) {
    /**
     * Factory method to create UserResponse from User entity
     */
    public static UserResponse fromUser(User user) {
        return new UserResponse(
            user.id(),
            user.username(),
            user.email(),
            user.firstName(),
            user.lastName(),
            user.emailVerified(),
            user.isActive(),
            user.roles() != null ? user.roles().split(",") : new String[0],
            null, // lastLoginAt not in User record
            user.createdAt(),
            user.updatedAt(),
            calculateProfileCompleteness(user),
            determineAccountStatus(user)
        );
    }

    /**
     * Calculate profile completion percentage
     */
    private static Integer calculateProfileCompleteness(User user) {
        int completed = 0;
        int total = 5;

        if (user.username() != null && !user.username().trim().isEmpty()) completed++;
        if (user.email() != null && !user.email().trim().isEmpty()) completed++;
        if (user.firstName() != null && !user.firstName().trim().isEmpty()) completed++;
        if (user.lastName() != null && !user.lastName().trim().isEmpty()) completed++;
        if (user.emailVerified() != null && user.emailVerified()) completed++;

        return (completed * 100) / total;
    }

    /**
     * Determine user account status
     */
    private static String determineAccountStatus(User user) {
        if (!user.isActive()) return "DISABLED";
        if (!user.emailVerified()) return "PENDING_VERIFICATION";
        return "ACTIVE";
    }

    /**
     * Get display name for user
     */
    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return username;
    }

    /**
     * Check if user profile is complete
     */
    public boolean isProfileComplete() {
        return profileCompleteness != null && profileCompleteness >= 100;
    }
}