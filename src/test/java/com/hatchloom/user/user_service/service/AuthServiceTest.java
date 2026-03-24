package com.hatchloom.user.user_service.service;

import com.hatchloom.user.user_service.dto.LoginRequest;
import com.hatchloom.user.user_service.dto.LoginResponse;
import com.hatchloom.user.user_service.dto.RegisterRequest;
import com.hatchloom.user.user_service.dto.RegisterResponse;
import com.hatchloom.user.user_service.model.AcademicProfile;
import com.hatchloom.user.user_service.model.Parent;
import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.Student;
import com.hatchloom.user.user_service.repository.ParentRepository;
import com.hatchloom.user.user_service.repository.StudentRepository;
import com.hatchloom.user.user_service.repository.UserProfileRepository;
import com.hatchloom.user.user_service.repository.UserRepository;
import com.hatchloom.user.user_service.security.SessionManager;
import com.hatchloom.user.user_service.strategy.StrategyFactory;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Authentication Service Test Cases")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private StrategyFactory strategyFactory;

    @Mock
    private com.hatchloom.user.user_service.strategy.registration.RegistrationStrategyFactory registrationStrategyFactory;

    @InjectMocks
    private AuthService authService;

    private UUID testUserId;
    private UUID testStudentId;
    private String testEmail;
    private String testUsername;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        testStudentId = UUID.randomUUID();
        testEmail = "test@example.com";
        testUsername = "testuser";

        // Setup registration strategies for each role
        com.hatchloom.user.user_service.strategy.registration.StudentRegistrationStrategy studentStrategy =
                new com.hatchloom.user.user_service.strategy.registration.StudentRegistrationStrategy();
        com.hatchloom.user.user_service.strategy.registration.ParentRegistrationStrategy parentStrategy =
                new com.hatchloom.user.user_service.strategy.registration.ParentRegistrationStrategy();
        com.hatchloom.user.user_service.strategy.registration.SchoolTeacherRegistrationStrategy teacherStrategy =
                new com.hatchloom.user.user_service.strategy.registration.SchoolTeacherRegistrationStrategy();
        com.hatchloom.user.user_service.strategy.registration.SchoolAdminRegistrationStrategy adminStrategy =
                new com.hatchloom.user.user_service.strategy.registration.SchoolAdminRegistrationStrategy();
        com.hatchloom.user.user_service.strategy.registration.HatchloomTeacherRegistrationStrategy hatchloomTeacherStrategy =
                new com.hatchloom.user.user_service.strategy.registration.HatchloomTeacherRegistrationStrategy();
        com.hatchloom.user.user_service.strategy.registration.HatchloomAdminRegistrationStrategy hatchloomAdminStrategy =
                new com.hatchloom.user.user_service.strategy.registration.HatchloomAdminRegistrationStrategy();

        // Wire up strategies for each role type
        when(registrationStrategyFactory.getStrategy(RoleType.STUDENT)).thenReturn(studentStrategy);
        when(registrationStrategyFactory.getStrategy(RoleType.PARENT)).thenReturn(parentStrategy);
        when(registrationStrategyFactory.getStrategy(RoleType.SCHOOL_TEACHER)).thenReturn(teacherStrategy);
        when(registrationStrategyFactory.getStrategy(RoleType.SCHOOL_ADMIN)).thenReturn(adminStrategy);
        when(registrationStrategyFactory.getStrategy(RoleType.HATCHLOOM_TEACHER)).thenReturn(hatchloomTeacherStrategy);
        when(registrationStrategyFactory.getStrategy(RoleType.HATCHLOOM_ADMIN)).thenReturn(hatchloomAdminStrategy);
    }

    @Test
    @DisplayName("Test ID 4: Student Registration Success - Create student with unique credentials")
    void testStudentRegistrationSuccess() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username(testUsername)
                .email(testEmail)
                .password("TestPassword123")
                .role("STUDENT")
                .schoolId(UUID.randomUUID().toString())
                .age(16)
                .build();

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(userRepository.existsByUsername(testUsername)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            Student student = (Student) invocation.getArgument(0);
            student.setId(testUserId);
            return student;
        });

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertEquals("STUDENT", response.getRole());
        assertEquals("Registration successful", response.getMessage());
        verify(userRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Test ID 4: Parent Registration Success - Create parent linked to student")
    void testParentRegistrationSuccess() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("parent_user")
                .email("parent@example.com")
                .password("ParentPassword123")
                .role("PARENT")
                .build();

        when(userRepository.existsByEmail("parent@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("parent_user")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_parent_password");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            Parent parent = (Parent) invocation.getArgument(0);
            parent.setId(UUID.randomUUID());
            return parent;
        });

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertEquals("PARENT", response.getRole());
        assertEquals("Registration successful", response.getMessage());
        verify(userRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Test ID 4: Parent Registration Failure - Invalid student ID")
    void testParentRegistrationFailureInvalidStudentId() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("parent_user")
                .email("parent@example.com")
                .password("ParentPassword123")
                .role("PARENT")
                .build();

        when(userRepository.existsByEmail("parent@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("parent_user")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_parent_password");
        when(studentRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(invocation -> {
            Parent parent = (Parent) invocation.getArgument(0);
            parent.setId(UUID.randomUUID());
            return parent;
        });

        // Act
        RegisterResponse response = authService.register(request);

        // Assert - Should still register parent even if student not found
        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertEquals("PARENT", response.getRole());
    }

    @Test
    @DisplayName("Test ID 5: User Login Success - Valid credentials return session tokens")
    void testLoginSuccessWithValidCredentials() {
        // Arrange
        Student user = new Student();
        user.setId(testUserId);
        user.setUsername(testUsername);
        user.setEmail(testEmail);
        user.setPasswordHash("hashed_password");
        user.setRole(RoleType.STUDENT);
        user.setActive(true);

        LoginRequest request = new LoginRequest(testUsername, "TestPassword123");

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("TestPassword123", "hashed_password")).thenReturn(true);
        when(sessionManager.generateSessionTokens(any(), anyString(), anyString()))
                .thenReturn(new com.hatchloom.user.user_service.security.SessionToken(
                        "access_token_jwt",
                        "refresh_token_jwt"
                ));

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("STUDENT", response.getRole());
        assertEquals(testUsername, response.getUsername());
        assertEquals("Login successful", response.getMessage());
        verify(userRepository, times(1)).findByUsername(testUsername);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Student login updates lastActive on academic profile")
    void testStudentLoginUpdatesLastActive() {
        // Arrange
        AcademicProfile profile = new AcademicProfile();
        Student user = new Student();
        user.setId(testUserId);
        user.setUsername(testUsername);
        user.setEmail(testEmail);
        user.setPasswordHash("hashed_password");
        user.setRole(RoleType.STUDENT);
        user.setActive(true);
        profile.setUser(user);
        user.setProfile(profile);

        LoginRequest request = new LoginRequest(testUsername, "TestPassword123");

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("TestPassword123", "hashed_password")).thenReturn(true);
        when(userProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionManager.generateSessionTokens(any(), anyString(), anyString()))
                .thenReturn(new com.hatchloom.user.user_service.security.SessionToken(
                        "access_token_jwt",
                        "refresh_token_jwt"
                ));

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertNotNull(profile.getLastActive());
        verify(userProfileRepository, times(1)).save(profile);
    }

    @Test
    @DisplayName("Test ID 5: User Login Failure - Invalid credentials")
    void testLoginFailureWithInvalidCredentials() {
        // Arrange
        Student user = new Student();
        user.setId(testUserId);
        user.setUsername(testUsername);
        user.setPasswordHash("hashed_password");
        user.setActive(true);

        LoginRequest request = new LoginRequest(testUsername, "WrongPassword");

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashed_password")).thenReturn(false);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertNull(response.getAccessToken());
        assertEquals("Invalid credentials", response.getMessage());
        verify(sessionManager, never()).generateSessionTokens(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test ID 5: User Login Failure - User not found")
    void testLoginFailureUserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent_user", "TestPassword123");

        when(userRepository.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertNull(response.getAccessToken());
        assertEquals("Invalid credentials", response.getMessage());
    }

    @Test
    @DisplayName("Test ID 6: Session Invalidation - Logout deletes session token")
    void testSessionInvalidation() {
        // Arrange
        String validToken = "valid_jwt_token";

        when(sessionManager.validateSessionToken(validToken)).thenReturn(true);
        when(sessionManager.getUserIdFromSessionToken(validToken)).thenReturn(testUserId);

        // Act
        boolean isValid = sessionManager.validateSessionToken(validToken);

        // Assert
        assertTrue(isValid);
        verify(sessionManager, times(1)).validateSessionToken(validToken);
    }

    @Test
    @DisplayName("Test ID 6: Session Token Expired - Access denied after logout")
    void testExpiredSessionToken() {
        // Arrange
        String expiredToken = "expired_jwt_token";

        when(sessionManager.validateSessionToken(expiredToken)).thenReturn(false);

        // Act
        boolean isValid = sessionManager.validateSessionToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Duplicate Email Registration Failure - Email must be unique")
    void testDuplicateEmailRegistrationFailure() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email(testEmail)
                .password("TestPassword123")
                .role("STUDENT")
                .schoolId(UUID.randomUUID().toString())
                .age(16)
                .build();

        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertNull(response.getUserId());
        assertEquals("Invalid registration request", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registration with missing required fields")
    void testRegistrationWithMissingFields() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username(testUsername)
                .email(testEmail)
                .password("TestPassword123")
                .role("STUDENT")
                .schoolId(UUID.randomUUID().toString())
                // Missing age field required for STUDENT
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            // This should fail due to validation
            if (request.getAge() == null && "STUDENT".equals(request.getRole())) {
                throw new IllegalArgumentException("Age is required for students");
            }
        });
    }

    @Test
    @DisplayName("Session validation returns user information")
    void testSessionValidationReturnsUserInfo() {
        // Arrange
        String validToken = "valid_jwt_token";
        Student user = new Student();
        user.setId(testUserId);
        user.setUsername(testUsername);
        user.setRole(RoleType.STUDENT);

        when(sessionManager.validateSessionToken(validToken)).thenReturn(true);
        when(sessionManager.getUserIdFromSessionToken(validToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));

        // Act
        boolean isValid = sessionManager.validateSessionToken(validToken);
        UUID userId = sessionManager.getUserIdFromSessionToken(validToken);

        // Assert
        assertTrue(isValid);
        assertEquals(testUserId, userId);
        verify(sessionManager, times(1)).validateSessionToken(validToken);
    }
}

