package com.enterprise.portfolio.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user login
 */
public record UserLoginRequest(
    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    String email,

    @JsonProperty("password")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be valid")
    String password
) {}