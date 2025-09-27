package com.enterprise.portfolio.userservice.controller;

import com.enterprise.portfolio.userservice.dto.*;
import com.enterprise.portfolio.userservice.service.JwtService;
import com.enterprise.portfolio.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication Controller - Handles user registration and login
 * 
 * Implements:
 * - Reactive REST endpoints with Spring WebFlux
 * - JWT token generation
 * - Input validation with Bean Validation
 * - Proper HTTP status codes and error handling
 * - API documentation ready structure
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserService userService;
    private final JwtService jwtService;
    
    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }
    
    /**
     * Register a new user
     * 
     * @param request User registration details
     * @return User response with created user information
     */
    @Operation(summary = "Register new user", description = "Creates a new user account")
    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        
        logger.info("Registration request received for username: {}", request.username());
        
        return userService.registerUser(request)
            .map(user -> ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user)))
            .onErrorResume(IllegalArgumentException.class, e ->
                Mono.just(ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()))))
            .onErrorResume(Exception.class, e -> {
                logger.error("Registration failed for username: {}", request.username(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed. Please try again.")));
            });
    }
    
    /**
     * Authenticate user and return JWT token
     * 
     * @param request User login credentials
     * @return Authentication response with JWT token and user information
     */
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<AuthenticationResponse>>> loginUser(
            @Valid @RequestBody UserLoginRequest request) {
        
        logger.info("Login request received for email: {}", request.email());
        
        return userService.authenticateUser(request)
            .map(user -> {
                String token = jwtService.generateToken(user);
                long expiresIn = jwtService.getTokenExpirationInSeconds();
                AuthenticationResponse authResponse = AuthenticationResponse.of(token, null, expiresIn, user);
                
                return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
            })
            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"))))
            .onErrorResume(Exception.class, e -> {
                logger.error("Login failed for email: {}", request.email(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed. Please try again.")));
            });
    }
    
    /**
     * Validate JWT token
     * 
     * @param token JWT token to validate
     * @return Token validation response
     */
    @PostMapping("/validate")
    public Mono<ResponseEntity<ApiResponse<java.util.Map<String, Object>>>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid authorization header")));
        }
        
        String token = authHeader.substring(7);
        
        if (jwtService.validateToken(token)) {
            java.util.Map<String, Object> claims = jwtService.getAllClaimsFromToken(token);
            return Mono.just(ResponseEntity.ok(ApiResponse.success("Token is valid", claims)));
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid or expired token")));
        }
    }
    
    /**
     * Refresh JWT token
     * 
     * @param authHeader Authorization header with current token
     * @return New JWT token if current token is valid
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<ApiResponse<AuthenticationResponse>>> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid authorization header")));
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtService.validateToken(token)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid or expired token")));
        }
        
        String username = jwtService.getUsernameFromToken(token);
        
        return userService.getUserByUsername(username)
            .map(user -> {
                String newToken = jwtService.generateToken(user);
                long expiresIn = jwtService.getTokenExpirationInSeconds();
                AuthenticationResponse authResponse = AuthenticationResponse.of(newToken, null, expiresIn, user);
                
                return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
            })
            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"))))
            .onErrorResume(Exception.class, e -> {
                logger.error("Token refresh failed for username: {}", username, e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Token refresh failed. Please try again.")));
            });
    }
}