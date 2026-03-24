package com.hatchloom.user.user_service.service;

import com.hatchloom.user.user_service.dto.*;
import com.hatchloom.user.user_service.model.*;
import com.hatchloom.user.user_service.repository.ParentRepository;
import com.hatchloom.user.user_service.repository.StudentRepository;
import com.hatchloom.user.user_service.repository.UserProfileRepository;
import com.hatchloom.user.user_service.repository.UserRepository;
import com.hatchloom.user.user_service.security.SessionManager;
import com.hatchloom.user.user_service.security.SessionToken;
import com.hatchloom.user.user_service.strategy.StrategyFactory;
import com.hatchloom.user.user_service.strategy.registration.RegistrationStrategyFactory;
import com.hatchloom.user.user_service.strategy.registration.RoleRegistrationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StrategyFactory strategyFactory;

    @Autowired
    private RegistrationStrategyFactory registrationStrategyFactory;

    public RegisterResponse register(RegisterRequest request) {
        if (!hasValidBaseRegistrationFields(request)) {
            return RegisterResponse.builder()
                    .message("Invalid registration request")
                    .build();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration attempt with existing email: {}", request.getEmail());
            return RegisterResponse.builder()
                    .message("Invalid registration request")
                    .build();
        }

        try {
            RoleType roleType = RoleType.valueOf(request.getRole().toUpperCase());
            RoleRegistrationStrategy strategy = registrationStrategyFactory.getStrategy(roleType);

            if (strategy == null || !strategy.isValid(request)) {
                log.warn("Registration rejected: invalid role payload for role={}", request.getRole());
                return RegisterResponse.builder()
                        .message("Invalid registration request")
                        .build();
            }

            String passwordHash = passwordEncoder.encode(request.getPassword());
            User user = strategy.createUser(request, passwordHash);

            if (user == null) {
                return RegisterResponse.builder()
                        .message("Invalid registration request")
                        .build();
            }

            user = userRepository.save(user);
            createDefaultProfileForUser(user);

            log.info("User registered successfully: {}", user.getUsername());
            return RegisterResponse.builder()
                    .userId(user.getId().toString())
                    .username(user.getUsername())
                    .role(user.getRole().toString())
                    .message("Registration successful")
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", request.getRole());
            return RegisterResponse.builder()
                    .message("Invalid registration request")
                    .build();
        } catch (Exception e) {
            log.error("Registration error", e);
            return RegisterResponse.builder()
                    .message("Invalid registration request")
                    .build();
        }
    }

    private boolean hasValidBaseRegistrationFields(RegisterRequest request) {
        if (request == null) {
            log.warn("Registration rejected: request is null");
            return false;
        }

        if (isBlank(request.getUsername()) || isBlank(request.getEmail()) || isBlank(request.getPassword()) || isBlank(request.getRole())) {
            log.warn("Registration rejected: missing base fields (username/email/password/role)");
            return false;
        }

        if (request.getPassword().length() < 5) {
            log.warn("Registration rejected: password too short for username={}", request.getUsername());
            return false;
        }

        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }


    public LoginResponse login(LoginRequest request) {
        try {
            User user = userRepository.findByUsername(request.getUsername()).orElse(null);

            if (user == null || !user.getActive()) {
                log.warn("Login attempt for non-existent or inactive user: {}", request.getUsername());
                return LoginResponse.builder()
                        .message("Invalid credentials")
                        .build();
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.warn("Failed login attempt for user: {}", request.getUsername());
                return LoginResponse.builder()
                        .message("Invalid credentials")
                        .build();
            }

            // Polymorphic login hook: each profile handles post-login actions (e.g., lastActive update)
            if (user.getProfile() != null) {
                user.getProfile().onUserLogin(user);
                userProfileRepository.save(user.getProfile());
            }

            SessionToken tokens = sessionManager.generateSessionTokens(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().toString()
            );

            log.info("User logged in successfully: {}", user.getUsername());

            return LoginResponse.builder()
                    .accessToken(tokens.getAccessToken())
                    .refreshToken(tokens.getRefreshToken())
                    .role(user.getRole().toString())
                    .username(user.getUsername())
                    .message("Login successful")
                    .build();
        } catch (Exception e) {
            log.error("Login error", e);
            return LoginResponse.builder()
                    .message("Invalid credentials")
                    .build();
        }
    }

    public LoginResponse refreshAccessToken(RefreshTokenRequest request) {
        try {
            SessionToken newTokens = sessionManager.refreshAccessToken(request.getRefreshToken());

            if (newTokens == null) {
                log.warn("Failed to refresh token");
                return LoginResponse.builder()
                        .message("Invalid refresh token")
                        .build();
            }

            User user = sessionManager.getUserFromSessionToken(newTokens.getAccessToken());

            return LoginResponse.builder()
                    .accessToken(newTokens.getAccessToken())
                    .refreshToken(newTokens.getRefreshToken())
                    .role(user.getRole().toString())
                    .username(user.getUsername())
                    .message("Token refreshed successfully")
                    .build();
        } catch (Exception e) {
            log.error("Token refresh error", e);
            return LoginResponse.builder()
                    .message("Invalid refresh token")
                    .build();
        }
    }

    public SessionValidationResponse validateSessionToken(String token) {
        try {
            boolean isValid = sessionManager.validateSessionToken(token);

            if (!isValid) {
                return SessionValidationResponse.builder()
                        .valid(false)
                        .message("Invalid or expired token")
                        .build();
            }

            UUID userId = sessionManager.getUserIdFromSessionToken(token);
            String role = sessionManager.getRoleFromSessionToken(token);

            return SessionValidationResponse.builder()
                    .valid(true)
                    .userId(userId.toString())
                    .role(role)
                    .message("Token is valid")
                    .build();
        } catch (Exception e) {
            log.error("Token validation error", e);
            return SessionValidationResponse.builder()
                    .valid(false)
                    .message("Invalid token")
                    .build();
        }
    }

    public RolePermissionDTO getRolePermissions(String token) {
        try {
            String role = sessionManager.getRoleFromSessionToken(token);

            if (role == null) {
                return null;
            }

            RoleType roleType = RoleType.valueOf(role);
            IRolePermissionStrategy strategy = strategyFactory.getStrategy(roleType);

            return RolePermissionDTO.builder()
                    .role(role)
                    .permissions(strategy.getPermissions())
                    .scope(strategy.getScope())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching role permissions", e);
            return null;
        }
    }

    public boolean linkParentToStudent(String parentToken, UUID studentId) {
        try {
            User parentUser = sessionManager.getUserFromSessionToken(parentToken);

            if (!(parentUser instanceof Parent)) {
                log.warn("Attempted parent linking by non-parent user");
                return false;
            }

            Student student = studentRepository.findById(studentId).orElse(null);

            if (student == null) {
                log.warn("Student not found for linking: {}", studentId);
                return false;
            }

            student.setParent((Parent) parentUser);
            studentRepository.save(student);

            log.info("Parent {} linked to student {}", parentUser.getId(), studentId);
            return true;
        } catch (Exception e) {
            log.error("Error linking parent to student", e);
            return false;
        }
    }

    private void createDefaultProfileForUser(User user) {
        UserProfile profile = null;

        if (user instanceof Student || user instanceof SchoolTeacher) {
            profile = new AcademicProfile();
        } else if (user instanceof SchoolAdmin || user instanceof HatchloomAdmin || user instanceof HatchloomTeacher) {
            profile = new ProfessionalProfile();
        } else if (user instanceof Parent) {
            profile = new ProfessionalProfile();
        }

        if (profile != null) {
            profile.setUser(user);
            profile.setBio("Bio");
            profile.setDescription("Description");
            profile.initializeDefaults(user);
            user.setProfile(profile);
            userRepository.save(user);
        }
    }
}
