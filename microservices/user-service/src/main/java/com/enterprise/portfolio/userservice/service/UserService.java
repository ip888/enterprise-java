package com.enterprise.portfolio.userservice.service;

import com.enterprise.portfolio.userservice.domain.User;
import com.enterprise.portfolio.userservice.dto.*;
import com.enterprise.portfolio.userservice.events.*;
import com.enterprise.portfolio.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User Service - Core business logic for user management
 * 
 * Implements:
 * - Reactive programming with Project Reactor
 * - Event-driven architecture with event publishing
 * - Comprehensive error handling
 * - Business logic separation
 * - Logging and monitoring
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    
    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Register a new user
     */
    public Mono<UserResponse> registerUser(UserRegistrationRequest request) {
        logger.info("Registering new user: {}", request.username());
        
        return validateUserDoesNotExist(request.username(), request.email())
            .then(createAndSaveUser(request))
            .doOnNext(user -> publishUserRegisteredEvent(user, generateCorrelationId()))
            .map(UserResponse::fromUser)
            .doOnSuccess(user -> logger.info("User registered successfully: {}", user.username()))
            .doOnError(error -> logger.error("Failed to register user: {}", request.username(), error));
    }
    
    /**
     * Authenticate user and return user details
     */
    public Mono<UserResponse> authenticateUser(UserLoginRequest request) {
        logger.info("Authenticating user: {}", request.email());
        
        return userRepository.findByUsernameOrEmail(request.email())
            .filter(user -> user.isActive())
            .filter(user -> passwordEncoder.matches(request.password(), user.passwordHash()))
            .map(UserResponse::fromUser)
            .doOnNext(user -> updateLastLogin(user.id()))
            .doOnSuccess(user -> logger.info("User authenticated successfully: {}", 
                user != null ? user.username() : "null"))
            .doOnError(error -> logger.warn("Authentication failed for user: {}", request.email()));
    }
    
    /**
     * Get user by ID
     */
    public Mono<UserResponse> getUserById(Long userId) {
        logger.debug("Fetching user by ID: {}", userId);
        
        return userRepository.findById(userId)
            .filter(user -> user.isActive())
            .map(UserResponse::fromUser)
            .doOnSuccess(user -> logger.debug("User found: {}", 
                user != null ? user.username() : "null"));
    }
    
    /**
     * Get user by username
     */
    public Mono<UserResponse> getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        
        return userRepository.findByUsernameIgnoreCase(username)
            .filter(user -> user.isActive())
            .map(UserResponse::fromUser);
    }
    
    /**
     * Update user information
     */
    public Mono<UserResponse> updateUser(Long userId, UserUpdateRequest request) {
        logger.info("Updating user: {}", userId);
        
        return userRepository.findById(userId)
            .filter(user -> user.isActive())
            .map(user -> user.withUpdatedInfo(
                request.firstName() != null ? request.firstName() : user.firstName(),
                request.lastName() != null ? request.lastName() : user.lastName(),
                request.email() != null ? request.email() : user.email()
            ))
            .flatMap(userRepository::save)
            .doOnNext(user -> publishUserUpdatedEvent(user, request, generateCorrelationId()))
            .map(UserResponse::fromUser)
            .doOnSuccess(user -> logger.info("User updated successfully: {}", user.username()))
            .doOnError(error -> logger.error("Failed to update user: {}", userId, error));
    }
    
    /**
     * Deactivate user (soft delete)
     */
    public Mono<Void> deactivateUser(Long userId, String reason) {
        logger.info("Deactivating user: {} with reason: {}", userId, reason);
        
        return userRepository.findById(userId)
            .filter(user -> user.isActive())
            .flatMap(user -> {
                User deactivatedUser = user.withActiveStatus(false);
                return userRepository.save(deactivatedUser)
                    .doOnNext(savedUser -> publishUserDeactivatedEvent(savedUser, reason, generateCorrelationId()));
            })
            .then()
            .doOnSuccess(v -> logger.info("User deactivated successfully: {}", userId))
            .doOnError(error -> logger.error("Failed to deactivate user: {}", userId, error));
    }
    
    /**
     * Verify user email
     */
    public Mono<UserResponse> verifyEmail(Long userId) {
        logger.info("Verifying email for user: {}", userId);
        
        return userRepository.findById(userId)
            .filter(user -> user.isActive())
            .filter(user -> !user.emailVerified())
            .map(user -> user.withEmailVerified(true))
            .flatMap(userRepository::save)
            .doOnNext(user -> publishUserEmailVerifiedEvent(user, generateCorrelationId()))
            .map(UserResponse::fromUser)
            .doOnSuccess(user -> logger.info("Email verified successfully for user: {}", user.username()))
            .doOnError(error -> logger.error("Failed to verify email for user: {}", userId, error));
    }
    
    /**
     * Search users by name or username
     */
    public Flux<UserResponse> searchUsers(String searchTerm, int limit, int offset) {
        logger.debug("Searching users with term: {}", searchTerm);
        
        return userRepository.searchUsers(searchTerm, limit, offset)
            .map(UserResponse::fromUser)
            .doOnComplete(() -> logger.debug("User search completed for term: {}", searchTerm));
    }
    
    /**
     * Get all active users (paginated)
     */
    public Flux<UserResponse> getAllActiveUsers() {
        logger.debug("Fetching all active users");
        
        return userRepository.findAllActiveUsers()
            .map(UserResponse::fromUser);
    }
    
    /**
     * Get user statistics
     */
    public Mono<java.util.Map<String, Long>> getUserStatistics() {
        logger.debug("Fetching user statistics");
        
        return Mono.zip(
            userRepository.countActiveUsers(),
            userRepository.countByEmailVerified(true),
            userRepository.countByEmailVerified(false)
        ).map(tuple -> java.util.Map.of(
            "totalActiveUsers", tuple.getT1(),
            "emailVerified", tuple.getT2(),
            "emailNotVerified", tuple.getT3()
        ));
    }
    
    // Helper methods
    
    private Mono<Void> validateUserDoesNotExist(String username, String email) {
        return Mono.zip(
            userRepository.existsByUsernameIgnoreCase(username),
            userRepository.existsByEmailIgnoreCase(email)
        ).flatMap(tuple -> {
            if (tuple.getT1()) {
                return Mono.error(new IllegalArgumentException("Username already exists"));
            }
            if (tuple.getT2()) {
                return Mono.error(new IllegalArgumentException("Email already exists"));
            }
            return Mono.empty();
        });
    }
    
    private Mono<User> createAndSaveUser(UserRegistrationRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User newUser = User.create(
            request.username(),
            request.email(),
            encodedPassword,
            request.firstName(),
            request.lastName()
        );
        
        return userRepository.save(newUser);
    }
    
    private void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId, java.time.LocalDateTime.now())
            .subscribe(
                v -> logger.debug("Last login updated for user: {}", userId),
                error -> logger.warn("Failed to update last login for user: {}", userId, error)
            );
    }
    
    private void publishUserRegisteredEvent(User user, String correlationId) {
        UserRegisteredEvent event = UserRegisteredEvent.create(
            user.id(), user.username(), user.email(), 
            user.firstName(), user.lastName(), correlationId
        );
        eventPublisher.publishUserEvent(event).subscribe();
    }
    
    private void publishUserUpdatedEvent(User user, UserUpdateRequest request, String correlationId) {
        java.util.List<String> updatedFields = new java.util.ArrayList<>();
        if (request.firstName() != null) updatedFields.add("firstName");
        if (request.lastName() != null) updatedFields.add("lastName");
        if (request.email() != null) updatedFields.add("email");
        
        UserUpdatedEvent event = UserUpdatedEvent.create(user.id(), updatedFields.toArray(new String[0]), correlationId);
        eventPublisher.publishUserEvent(event).subscribe();
    }
    
    private void publishUserDeactivatedEvent(User user, String reason, String correlationId) {
        UserDeactivatedEvent event = UserDeactivatedEvent.create(user.id(), reason, correlationId);
        eventPublisher.publishUserEvent(event).subscribe();
    }
    
    private void publishUserEmailVerifiedEvent(User user, String correlationId) {
        UserEmailVerifiedEvent event = UserEmailVerifiedEvent.create(user.id(), user.email(), "email-link", correlationId);
        eventPublisher.publishUserEvent(event).subscribe();
    }
    
    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }
}