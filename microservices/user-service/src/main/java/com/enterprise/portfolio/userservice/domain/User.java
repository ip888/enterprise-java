package com.enterprise.portfolio.userservice.domain;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * User Entity using modern Java Record for immutability and conciseness
 * 
 * Implements:
 * - Java 21 Record syntax
 * - Spring Data R2DBC annotations
 * - Bean validation
 * - Immutable data structures
 */
@Table("users")
public record User(
    @Id
    Long id,
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column("username")
    String username,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column("email")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column("password_hash")
    String passwordHash,
    
    @NotBlank(message = "First name is required")
    @Column("first_name")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    @Column("last_name")
    String lastName,
    
    @Column("phone_number")
    String phoneNumber,
    
    @Column("is_active")
    Boolean isActive,
    
    @Column("email_verified")
    Boolean emailVerified,
    
    @Column("roles")
    String roles, // JSON string for roles array
    
    @CreatedDate
    @Column("created_at")
    LocalDateTime createdAt,
    
    @LastModifiedDate
    @Column("updated_at")
    LocalDateTime updatedAt,
    
    @Version
    @Column("version")
    Long version
) {
    
    /**
     * Factory method for creating a new user
     */
    public static User create(String username, String email, String passwordHash, 
                             String firstName, String lastName) {
        return new User(
            null, // ID will be generated
            username,
            email,
            passwordHash,
            firstName,
            lastName,
            null, // phoneNumber
            true, // isActive
            false, // emailVerified
            "[\"USER\"]", // default role
            null, // createdAt will be set by @CreatedDate
            null, // updatedAt will be set by @LastModifiedDate
            null  // version will be managed by @Version
        );
    }
    
    /**
     * Helper method to update user information
     */
    public User withUpdatedInfo(String firstName, String lastName, String phoneNumber) {
        return new User(
            id, username, email, passwordHash,
            firstName, lastName, phoneNumber,
            isActive, emailVerified, roles,
            createdAt, updatedAt, version
        );
    }
    
    /**
     * Helper method to activate/deactivate user
     */
    public User withActiveStatus(boolean active) {
        return new User(
            id, username, email, passwordHash,
            firstName, lastName, phoneNumber,
            active, emailVerified, roles,
            createdAt, updatedAt, version
        );
    }
    
    /**
     * Helper method to verify email
     */
    public User withEmailVerified(boolean verified) {
        return new User(
            id, username, email, passwordHash,
            firstName, lastName, phoneNumber,
            isActive, verified, roles,
            createdAt, updatedAt, version
        );
    }
    
    /**
     * Get user roles as Set
     */
    public Set<String> getRolesSet() {
        // Simple implementation - in production, use proper JSON parsing
        if (roles == null || roles.isEmpty()) {
            return Set.of("USER");
        }
        
        return Set.of(roles.replace("[", "").replace("]", "").replace("\"", "").split(","));
    }
    
    /**
     * Get full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(String role) {
        return getRolesSet().contains(role);
    }
}