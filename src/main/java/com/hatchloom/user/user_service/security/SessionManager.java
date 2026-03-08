package com.hatchloom.user.user_service.security;

import com.hatchloom.user.user_service.model.User;
import com.hatchloom.user.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class SessionManager {

    private static SessionManager instance;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Autowired
    public SessionManager(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        SessionManager.instance = this;
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public SessionToken generateSessionTokens(UUID userId, String username, String role) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, username, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, username);
        return new SessionToken(accessToken, refreshToken);
    }

    public boolean validateSessionToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return jwtTokenProvider.validateToken(token);
    }

    public UUID getUserIdFromSessionToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    public String getRoleFromSessionToken(String token) {
        return jwtTokenProvider.getRoleFromToken(token);
    }

    public User getUserFromSessionToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("Null or empty token provided to getUserFromSessionToken");
            return null;
        }

        try {
            UUID userId = getUserIdFromSessionToken(token);
            if (userId != null) {
                return userRepository.findById(userId).orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting user from token", e);
            return null;
        }
    }

    public SessionToken refreshAccessToken(String refreshToken) {
        if (!validateSessionToken(refreshToken)) {
            log.warn("Invalid or expired refresh token");
            return null;
        }

        String tokenType = jwtTokenProvider.getTokenTypeFromToken(refreshToken);
        if (!"REFRESH".equals(tokenType)) {
            log.warn("Token is not a refresh token");
            return null;
        }

        UUID userId = getUserIdFromSessionToken(refreshToken);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            log.warn("User not found for refresh token");
            return null;
        }

        return generateSessionTokens(userId, user.getUsername(), user.getRole().toString());
    }
}

