package com.enterprise.portfolio.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class CacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    // Cache TTL configurations
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);
    private static final Duration USER_PROFILE_TTL = Duration.ofMinutes(30);
    private static final Duration AUTH_TOKEN_TTL = Duration.ofHours(1);
    
    @Autowired
    public CacheService(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Cache user profile data
     */
    public Mono<Boolean> cacheUserProfile(String userId, Object userData) {
        String key = "user:profile:" + userId;
        return redisTemplate.opsForValue()
            .set(key, userData, USER_PROFILE_TTL);
    }
    
    /**
     * Get cached user profile
     */
    public Mono<Object> getCachedUserProfile(String userId) {
        String key = "user:profile:" + userId;
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * Cache authentication token validation result
     */
    public Mono<Boolean> cacheTokenValidation(String tokenHash, Boolean isValid) {
        String key = "auth:token:" + tokenHash;
        return redisTemplate.opsForValue()
            .set(key, isValid, AUTH_TOKEN_TTL);
    }
    
    /**
     * Get cached token validation result
     */
    public Mono<Boolean> getCachedTokenValidation(String tokenHash) {
        String key = "auth:token:" + tokenHash;
        return redisTemplate.opsForValue().get(key)
            .cast(Boolean.class)
            .defaultIfEmpty(false);
    }
    
    /**
     * Cache user session data
     */
    public Mono<Boolean> cacheUserSession(String sessionId, Map<String, Object> sessionData) {
        String key = "session:" + sessionId;
        return redisTemplate.opsForHash()
            .putAll(key, sessionData)
            .then(redisTemplate.expire(key, DEFAULT_TTL));
    }
    
    /**
     * Get cached user session
     */
    public Mono<Map<Object, Object>> getCachedUserSession(String sessionId) {
        String key = "session:" + sessionId;
        return redisTemplate.opsForHash().entries(key)
            .collectMap(entry -> entry.getKey(), entry -> entry.getValue());
    }
    
    /**
     * Invalidate user cache
     */
    public Mono<Boolean> invalidateUserCache(String userId) {
        String profileKey = "user:profile:" + userId;
        return redisTemplate.delete(profileKey)
            .map(count -> count > 0);
    }
    
    /**
     * Cache user search results
     */
    public Mono<Boolean> cacheSearchResults(String searchTerm, Object results) {
        String key = "search:users:" + searchTerm.toLowerCase();
        return redisTemplate.opsForValue()
            .set(key, results, Duration.ofMinutes(5)); // Short TTL for search results
    }
    
    /**
     * Get cached search results
     */
    public Mono<Object> getCachedSearchResults(String searchTerm) {
        String key = "search:users:" + searchTerm.toLowerCase();
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * Test Redis connectivity
     */
    public Mono<String> ping() {
        return redisTemplate.opsForValue()
            .get("ping-test")
            .then(Mono.just("Redis connection successful"))
            .onErrorReturn("Redis connection failed");
    }
    
    /**
     * Get Redis server info for health check
     */
    public Mono<String> getRedisInfo() {
        return redisTemplate.opsForValue()
            .get("redis-info-test")
            .then(Mono.just("Redis available"))
            .onErrorReturn("Unable to get Redis info");
    }
    
    /**
     * Clear all user-related caches
     */
    public Mono<Long> clearUserCaches() {
        return redisTemplate.scan()
            .filter(key -> 
                key.startsWith("user:") || 
                key.startsWith("session:") || 
                key.startsWith("search:")
            )
            .collectList()
            .flatMap(keys -> {
                if (keys.isEmpty()) {
                    return Mono.just(0L);
                }
                return redisTemplate.delete(keys.toArray(new String[0]));
            });
    }
}