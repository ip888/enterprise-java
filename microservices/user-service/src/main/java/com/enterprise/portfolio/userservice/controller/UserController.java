package com.enterprise.portfolio.userservice.controller;

import com.enterprise.portfolio.userservice.dto.*;
import com.enterprise.portfolio.userservice.service.JwtService;
import com.enterprise.portfolio.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User Management Controller - Handles user CRUD operations
 * 
 * Implements:
 * - RESTful API design with proper HTTP methods
 * - Reactive programming with Flux and Mono
 * - JWT-based authorization
 * - Comprehensive error handling
 * - Input validation and sanitization
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "User Management", description = "User profile and management operations")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    private final JwtService jwtService;
    
    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }
    
    /**
     * Get current user profile
     * 
     * @param authHeader Authorization header with JWT token
     * @return Current user's profile information
     */
    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        return extractUserIdFromToken(authHeader)
            .flatMap(userService::getUserById)
            .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"))))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Get user by ID
     * 
     * @param userId User ID
     * @return User profile information
     */
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUserById(@PathVariable Long userId) {
        
        logger.debug("Fetching user with ID: {}", userId);
        
        return userService.getUserById(userId)
            .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"))))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Get user by username
     * 
     * @param username Username
     * @return User profile information
     */
    @GetMapping("/username/{username}")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUserByUsername(@PathVariable String username) {
        
        logger.debug("Fetching user with username: {}", username);
        
        return userService.getUserByUsername(username)
            .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"))))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Update current user profile
     * 
     * @param authHeader Authorization header with JWT token
     * @param request User update request
     * @return Updated user profile information
     */
    @PutMapping("/me")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> updateCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateRequest request) {
        
        return extractUserIdFromToken(authHeader)
            .flatMap(userId -> userService.updateUser(userId, request))
            .map(user -> ResponseEntity.ok(ApiResponse.success("User updated successfully", user)))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Update user by ID (Admin only - for demonstration)
     * 
     * @param userId User ID
     * @param request User update request
     * @return Updated user profile information
     */
    @PutMapping("/{userId}")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        
        logger.info("Updating user with ID: {}", userId);
        
        return userService.updateUser(userId, request)
            .map(user -> ResponseEntity.ok(ApiResponse.success("User updated successfully", user)))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Deactivate current user account
     * 
     * @param authHeader Authorization header with JWT token
     * @return Confirmation message
     */
    @DeleteMapping("/me")
    public Mono<ResponseEntity<ApiResponse<String>>> deactivateCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        return extractUserIdFromToken(authHeader)
            .flatMap(userId -> userService.deactivateUser(userId, "User requested account deactivation"))
            .then(Mono.just(ResponseEntity.ok(ApiResponse.success("Account deactivated successfully", "Account has been deactivated"))))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Deactivate user by ID (Admin only - for demonstration)
     * 
     * @param userId User ID
     * @return Confirmation message
     */
    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<ApiResponse<String>>> deactivateUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "Admin deactivation") String reason) {
        
        logger.info("Deactivating user with ID: {} for reason: {}", userId, reason);
        
        return userService.deactivateUser(userId, reason)
            .then(Mono.just(ResponseEntity.ok(ApiResponse.success("User deactivated successfully", "User has been deactivated"))))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Verify email for current user
     * 
     * @param authHeader Authorization header with JWT token
     * @return Updated user profile with verified email
     */
    @PostMapping("/me/verify-email")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> verifyCurrentUserEmail(
            @RequestHeader("Authorization") String authHeader) {
        
        return extractUserIdFromToken(authHeader)
            .flatMap(userService::verifyEmail)
            .map(user -> ResponseEntity.ok(ApiResponse.success("Email verified successfully", user)))
            .switchIfEmpty(Mono.just(ResponseEntity.badRequest()
                .body(ApiResponse.error("Email already verified or user not found"))))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Search users by name or username
     * 
     * @param searchTerm Search term
     * @param limit Number of results to return (default: 20)
     * @param offset Number of results to skip (default: 0)
     * @return List of matching users
     */
    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponse<java.util.List<UserResponse>>>> searchUsers(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        logger.debug("Searching users with term: {}", searchTerm);
        
        return userService.searchUsers(searchTerm, limit, offset)
            .collectList()
            .map(users -> ResponseEntity.ok(ApiResponse.success(users)))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Get all active users (for demonstration - would typically be admin-only)
     * 
     * @return List of all active users
     */
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<java.util.List<UserResponse>>>> getAllUsers() {
        
        logger.debug("Fetching all active users");
        
        return userService.getAllActiveUsers()
            .collectList()
            .map(users -> ResponseEntity.ok(ApiResponse.success(users)))
            .onErrorResume(this::handleError);
    }
    
    /**
     * Get user statistics
     * 
     * @return User statistics
     */
    @GetMapping("/statistics")
    public Mono<ResponseEntity<ApiResponse<java.util.Map<String, Long>>>> getUserStatistics() {
        
        logger.debug("Fetching user statistics");
        
        return userService.getUserStatistics()
            .map(stats -> ResponseEntity.ok(ApiResponse.success(stats)))
            .onErrorResume(this::handleError);
    }
    
    // Helper methods
    
    private Mono<Long> extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new IllegalArgumentException("Invalid authorization header"));
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtService.validateToken(token)) {
            return Mono.error(new IllegalArgumentException("Invalid or expired token"));
        }
        
        return Mono.just(jwtService.getUserIdFromToken(token));
    }
    
    private <T> Mono<ResponseEntity<ApiResponse<T>>> handleError(Throwable error) {
        logger.error("Error in UserController", error);
        
        if (error instanceof IllegalArgumentException) {
            return Mono.just(ResponseEntity.badRequest()
                .body(ApiResponse.error(error.getMessage())));
        }
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An error occurred. Please try again.")));
    }
}