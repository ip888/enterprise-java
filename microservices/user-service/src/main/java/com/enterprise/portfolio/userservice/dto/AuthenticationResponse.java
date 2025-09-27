package com.enterprise.portfolio.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Response DTO for authentication operations
 */
public record AuthenticationResponse(
    @JsonProperty("accessToken")
    @NotBlank
    String accessToken,

    @JsonProperty("refreshToken")
    String refreshToken,

    @JsonProperty("tokenType")
    String tokenType,

    @JsonProperty("expiresIn")
    Long expiresIn,

    @JsonProperty("user")
    UserResponse user
) {
    /**
     * Create authentication response
     */
    public static AuthenticationResponse of(String accessToken, String refreshToken, 
                                          Long expiresIn, UserResponse user) {
        return new AuthenticationResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}