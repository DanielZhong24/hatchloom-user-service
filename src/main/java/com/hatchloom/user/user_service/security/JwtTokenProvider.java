package com.hatchloom.user.user_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry-minutes}")
    private int accessTokenExpiryMinutes;

    @Value("${jwt.refresh-token-expiry-days}")
    private int refreshTokenExpiryDays;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(UUID userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", "ACCESS");
        return createToken(claims, userId.toString(), getAccessTokenExpiry());
    }

    public String generateRefreshToken(UUID userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");
        return createToken(claims, userId.toString(), getRefreshTokenExpiry());
    }

    private String createToken(Map<String, Object> claims, String subject, long expiryMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMillis);

        return Jwts.builder()
                .claims().add(claims).and()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            log.error("Failed to get user ID from token", e);
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return (String) claims.get("role");
        } catch (Exception e) {
            log.error("Failed to get role from token", e);
            return null;
        }
    }

    public String getTokenTypeFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return (String) claims.get("type");
        } catch (Exception e) {
            log.error("Failed to get token type from token", e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private long getAccessTokenExpiry() {
        return (long) accessTokenExpiryMinutes * 60 * 1000;
    }

    private long getRefreshTokenExpiry() {
        return (long) refreshTokenExpiryDays * 24 * 60 * 60 * 1000;
    }
}

