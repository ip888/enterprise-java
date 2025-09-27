package com.enterprise.portfolio.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user updates
 */
public record UserUpdateRequest(
    @JsonProperty("firstName")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    String firstName,

    @JsonProperty("lastName")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    String lastName,

    @JsonProperty("email")
    @Email(message = "Valid email is required")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email
) {}