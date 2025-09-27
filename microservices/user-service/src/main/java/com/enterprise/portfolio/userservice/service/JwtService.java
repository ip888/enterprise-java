package com.enterprise.portfolio.userservice.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.enterprise.portfolio.userservice.dto.UserResponse;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * JWT Service for token generation and validation
 * 
 * Implements:
 * - JSON Web Token (JWT) implementation
 * - Secure token generation and validation
 * - Claims-based authorization
 * - Token expiration handling
 */
@Service
public class JwtService {
    
    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    private final String jwtIssuer;
    
    public JwtService(
        @Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}") String jwtSecret,
        @Value("${app.jwt.expiration:86400000}") long jwtExpirationMs, // 24 hours
        @Value("${app.jwt.issuer:user-service}") String jwtIssuer) {
        
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtIssuer = jwtIssuer;
    }
    
    /**
     * Generate JWT token for authenticated user
     */
    public String generateToken(UserResponse user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
            .subject(user.username())
            .claim("userId", user.id())
            .claim("email", user.email())
            .claim("roles", user.roles())
            .claim("emailVerified", user.emailVerified())
            .issuer(jwtIssuer)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }
    
    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
    
    /**
     * Extract user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * Extract all claims from JWT token
     */
    public Map<String, Object> getAllClaimsFromToken(String token) {
        Claims claims = parseToken(token);
        return Map.of(
            "username", claims.getSubject(),
            "userId", claims.get("userId", Long.class),
            "email", claims.get("email", String.class),
            "roles", claims.get("roles"),
            "emailVerified", claims.get("emailVerified", Boolean.class),
            "issuer", claims.getIssuer(),
            "issuedAt", claims.getIssuedAt(),
            "expiration", claims.getExpiration()
        );
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
    
    /**
     * Get token expiration date
     */
    public LocalDateTime getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
    
    /**
     * Get time until token expires (in milliseconds)
     */
    public long getTimeUntilExpiration(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
    
    /**
     * Get token expiration duration in seconds
     */
    public long getTokenExpirationInSeconds() {
        return jwtExpirationMs / 1000;
    }
    
    /**
     * Parse and validate JWT token
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .requireIssuer(jwtIssuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}