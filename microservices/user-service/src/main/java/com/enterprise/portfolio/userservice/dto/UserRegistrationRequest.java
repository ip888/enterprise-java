package com.enterprise.portfolio.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration
 */
public record UserRegistrationRequest(
    @JsonProperty("username")
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores, and hyphens")
    String username,

    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,

    @JsonProperty("password")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "Password must contain at least one lowercase, uppercase, digit, and special character")
    String password,

    @JsonProperty("firstName")
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    String firstName,

    @JsonProperty("lastName")
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    String lastName
) {
    /**
     * Business validation for registration data
     */
    public boolean isValidForRegistration() {
        return username != null && email != null && password != null &&
               firstName != null && lastName != null &&
               !username.trim().isEmpty() && !email.trim().isEmpty();
    }
}