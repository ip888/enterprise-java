package com.enterprise.portfolio.userservice.repository;

import com.enterprise.portfolio.userservice.domain.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive User Repository using Spring Data R2DBC
 * 
 * Implements:
 * - Reactive database operations with R2DBC
 * - Custom queries with @Query annotation
 * - Non-blocking database access
 * - Type-safe repository methods
 */
@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    
    /**
     * Find user by username (case-insensitive)
     */
    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username)")
    Mono<User> findByUsernameIgnoreCase(String username);
    
    /**
     * Find user by email (case-insensitive)
     */
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email)")
    Mono<User> findByEmailIgnoreCase(String email);
    
    /**
     * Find user by username or email
     */
    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:usernameOrEmail) OR LOWER(email) = LOWER(:usernameOrEmail)")
    Mono<User> findByUsernameOrEmail(String usernameOrEmail);
    
    /**
     * Check if username exists (case-insensitive)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(username) = LOWER(:username))")
    Mono<Boolean> existsByUsernameIgnoreCase(String username);
    
    /**
     * Check if email exists (case-insensitive)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(:email))")
    Mono<Boolean> existsByEmailIgnoreCase(String email);
    
    /**
     * Find all active users
     */
    @Query("SELECT * FROM users WHERE is_active = true ORDER BY created_at DESC")
    Flux<User> findAllActiveUsers();
    
    /**
     * Find users by role
     */
    @Query("SELECT * FROM users WHERE roles LIKE %:role% AND is_active = true")
    Flux<User> findByRole(String role);
    
    /**
     * Find users created after a specific date
     */
    @Query("SELECT * FROM users WHERE created_at >= :fromDate ORDER BY created_at DESC")
    Flux<User> findUsersCreatedAfter(java.time.LocalDateTime fromDate);
    
    /**
     * Count active users
     */
    @Query("SELECT COUNT(*) FROM users WHERE is_active = true")
    Mono<Long> countActiveUsers();
    
    /**
     * Count users by email verification status
     */
    @Query("SELECT COUNT(*) FROM users WHERE email_verified = :verified")
    Mono<Long> countByEmailVerified(boolean verified);
    
    /**
     * Find users by name pattern (for search functionality)
     */
    @Query("SELECT * FROM users WHERE " +
           "(LOWER(first_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(last_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND is_active = true " +
           "ORDER BY created_at DESC " +
           "LIMIT :limit OFFSET :offset")
    Flux<User> searchUsers(String searchTerm, int limit, int offset);
    
    /**
     * Update user's last login timestamp
     */
    @Query("UPDATE users SET updated_at = :lastLogin WHERE id = :userId")
    Mono<Void> updateLastLogin(Long userId, java.time.LocalDateTime lastLogin);
    
    /**
     * Soft delete user (deactivate instead of hard delete)
     */
    @Query("UPDATE users SET is_active = false, updated_at = NOW() WHERE id = :userId")
    Mono<Void> deactivateUser(Long userId);
    
    /**
     * Verify user email
     */
    @Query("UPDATE users SET email_verified = true, updated_at = NOW() WHERE id = :userId")
    Mono<Void> verifyEmail(Long userId);
}