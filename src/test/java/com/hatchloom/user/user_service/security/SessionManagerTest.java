package com.hatchloom.user.user_service.security;

import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.Student;
import com.hatchloom.user.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Session Management Test Cases")
class SessionManagerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionManager sessionManager;

    private UUID testUserId;
    private String testUsername;
    private String testRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        testUsername = "testuser";
        testRole = "STUDENT";
    }

    @Test
    @DisplayName("Test ID 5: SessionManager generates session tokens on login")
    void testGenerateSessionTokens() {
        // Arrange
        String expectedAccessToken = "access_token_jwt_value";
        String expectedRefreshToken = "refresh_token_jwt_value";

        when(jwtTokenProvider.generateAccessToken(testUserId, testUsername, testRole))
                .thenReturn(expectedAccessToken);
        when(jwtTokenProvider.generateRefreshToken(testUserId, testUsername))
                .thenReturn(expectedRefreshToken);

        // Act
        SessionToken tokens = sessionManager.generateSessionTokens(testUserId, testUsername, testRole);

        // Assert
        assertNotNull(tokens);
        assertEquals(expectedAccessToken, tokens.getAccessToken());
        assertEquals(expectedRefreshToken, tokens.getRefreshToken());
        verify(jwtTokenProvider, times(1)).generateAccessToken(testUserId, testUsername, testRole);
        verify(jwtTokenProvider, times(1)).generateRefreshToken(testUserId, testUsername);
    }

    @Test
    @DisplayName("Test ID 6: Session validation with valid token")
    void testValidateSessionTokenValid() {
        // Arrange
        String validToken = "valid_jwt_token";

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

        // Act
        boolean isValid = sessionManager.validateSessionToken(validToken);

        // Assert
        assertTrue(isValid);
        verify(jwtTokenProvider, times(1)).validateToken(validToken);
    }

    @Test
    @DisplayName("Test ID 6: Session validation with invalid token")
    void testValidateSessionTokenInvalid() {
        // Arrange
        String invalidToken = "invalid_jwt_token";

        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        boolean isValid = sessionManager.validateSessionToken(invalidToken);

        // Assert
        assertFalse(isValid);
        verify(jwtTokenProvider, times(1)).validateToken(invalidToken);
    }

    @Test
    @DisplayName("Test ID 6: Session validation with null token")
    void testValidateSessionTokenNull() {
        // Act
        boolean isValid = sessionManager.validateSessionToken(null);

        // Assert
        assertFalse(isValid);
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    @Test
    @DisplayName("Test ID 6: Session validation with empty token")
    void testValidateSessionTokenEmpty() {
        // Act
        boolean isValid = sessionManager.validateSessionToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Extract user ID from valid session token")
    void testGetUserIdFromSessionToken() {
        // Arrange
        String validToken = "valid_jwt_token";

        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(testUserId);

        // Act
        UUID userId = sessionManager.getUserIdFromSessionToken(validToken);

        // Assert
        assertNotNull(userId);
        assertEquals(testUserId, userId);
        verify(jwtTokenProvider, times(1)).getUserIdFromToken(validToken);
    }

    @Test
    @DisplayName("Extract role from valid session token")
    void testGetRoleFromSessionToken() {
        // Arrange
        String validToken = "valid_jwt_token";

        when(jwtTokenProvider.getRoleFromToken(validToken)).thenReturn(testRole);

        // Act
        String role = sessionManager.getRoleFromSessionToken(validToken);

        // Assert
        assertNotNull(role);
        assertEquals(testRole, role);
        verify(jwtTokenProvider, times(1)).getRoleFromToken(validToken);
    }

    @Test
    @DisplayName("Test ID 6: Retrieve user object from session token")
    void testGetUserFromSessionToken() {
        // Arrange
        String validToken = "valid_jwt_token";
        Student user = new Student();
        user.setId(testUserId);
        user.setUsername(testUsername);
        user.setEmail("test@example.com");
        user.setRole(RoleType.STUDENT);

        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));

        // Act
        com.hatchloom.user.user_service.model.User retrievedUser = sessionManager.getUserFromSessionToken(validToken);

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(testUserId, retrievedUser.getId());
        assertEquals(testUsername, retrievedUser.getUsername());
        verify(jwtTokenProvider, times(1)).getUserIdFromToken(validToken);
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Test ID 6: Refresh access token with valid refresh token")
    void testRefreshAccessTokenSuccess() {
        // Arrange
        String refreshToken = "refresh_token_jwt";
        String newAccessToken = "new_access_token";
        String newRefreshToken = "new_refresh_token";

        Student user = new Student();
        user.setId(testUserId);
        user.setUsername(testUsername);
        user.setRole(RoleType.STUDENT);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenTypeFromToken(refreshToken)).thenReturn("REFRESH");
        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(testUserId, testUsername, "STUDENT"))
                .thenReturn(newAccessToken);
        when(jwtTokenProvider.generateRefreshToken(testUserId, testUsername))
                .thenReturn(newRefreshToken);

        // Act
        SessionToken newTokens = sessionManager.refreshAccessToken(refreshToken);

        // Assert
        assertNotNull(newTokens);
        assertEquals(newAccessToken, newTokens.getAccessToken());
        assertEquals(newRefreshToken, newTokens.getRefreshToken());
        verify(jwtTokenProvider, times(1)).validateToken(refreshToken);
        verify(jwtTokenProvider, times(1)).getTokenTypeFromToken(refreshToken);
    }

    @Test
    @DisplayName("Refresh token fails with invalid token")
    void testRefreshAccessTokenFailureInvalidToken() {
        // Arrange
        String invalidRefreshToken = "invalid_refresh_token";

        when(jwtTokenProvider.validateToken(invalidRefreshToken)).thenReturn(false);

        // Act
        SessionToken newTokens = sessionManager.refreshAccessToken(invalidRefreshToken);

        // Assert
        assertNull(newTokens);
        verify(jwtTokenProvider, times(1)).validateToken(invalidRefreshToken);
    }

    @Test
    @DisplayName("Refresh token fails with access token instead of refresh token")
    void testRefreshAccessTokenFailureWrongTokenType() {
        // Arrange
        String accessToken = "access_token_instead_of_refresh";

        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenTypeFromToken(accessToken)).thenReturn("ACCESS");

        // Act
        SessionToken newTokens = sessionManager.refreshAccessToken(accessToken);

        // Assert
        assertNull(newTokens);
        verify(jwtTokenProvider, times(1)).validateToken(accessToken);
        verify(jwtTokenProvider, times(1)).getTokenTypeFromToken(accessToken);
    }

    @Test
    @DisplayName("Refresh token fails when user not found")
    void testRefreshAccessTokenFailureUserNotFound() {
        // Arrange
        String refreshToken = "refresh_token_jwt";

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenTypeFromToken(refreshToken)).thenReturn("REFRESH");
        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act
        SessionToken newTokens = sessionManager.refreshAccessToken(refreshToken);

        // Assert
        assertNull(newTokens);
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("SessionManager is singleton")
    void testSessionManagerSingleton() {
        // Act
        SessionManager instance1 = SessionManager.getInstance();
        SessionManager instance2 = SessionManager.getInstance();

        // Assert
        assertSame(instance1, instance2);
    }
}

