package com.hatchloom.user.user_service.service;

import com.hatchloom.user.user_service.dto.*;
import com.hatchloom.user.user_service.model.*;
import com.hatchloom.user.user_service.repository.ParentRepository;
import com.hatchloom.user.user_service.repository.StudentRepository;
import com.hatchloom.user.user_service.repository.UserRepository;
import com.hatchloom.user.user_service.security.SessionManager;
import com.hatchloom.user.user_service.security.SessionToken;
import com.hatchloom.user.user_service.strategy.StrategyFactory;
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
    private SessionManager sessionManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StrategyFactory strategyFactory;

    public RegisterResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration attempt with existing email: {}", request.getEmail());
            return RegisterResponse.builder()
                    .message("Invalid registration request")
                    .build();
        }

        try {
            RoleType roleType = RoleType.valueOf(request.getRole().toUpperCase());
            User user = createUserByRole(roleType, request);

            if (user == null) {
                return RegisterResponse.builder()
                        .message("Invalid registration request")
                        .build();
            }

            user = userRepository.save(user);

            // Create default profile
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

    private User createUserByRole(RoleType roleType, RegisterRequest request) {
        String passwordHash = passwordEncoder.encode(request.getPassword());

        return switch (roleType) {
            case STUDENT -> {
                Student student = new Student();
                student.setUsername(request.getUsername());
                student.setEmail(request.getEmail());
                student.setPasswordHash(passwordHash);
                student.setRole(roleType);
                student.setSchoolId(UUID.fromString(request.getSchoolId()));
                student.setAge(request.getAge());
                yield student;
            }
            case SCHOOL_TEACHER -> {
                SchoolTeacher teacher = new SchoolTeacher();
                teacher.setUsername(request.getUsername());
                teacher.setEmail(request.getEmail());
                teacher.setPasswordHash(passwordHash);
                teacher.setRole(roleType);
                teacher.setSchoolId(UUID.fromString(request.getSchoolId()));
                yield teacher;
            }
            case SCHOOL_ADMIN -> {
                SchoolAdmin admin = new SchoolAdmin();
                admin.setUsername(request.getUsername());
                admin.setEmail(request.getEmail());
                admin.setPasswordHash(passwordHash);
                admin.setRole(roleType);
                admin.setSchoolId(UUID.fromString(request.getSchoolId()));
                yield admin;
            }
            case HATCHLOOM_TEACHER -> {
                HatchloomTeacher teacher = new HatchloomTeacher();
                teacher.setUsername(request.getUsername());
                teacher.setEmail(request.getEmail());
                teacher.setPasswordHash(passwordHash);
                teacher.setRole(roleType);
                yield teacher;
            }
            case HATCHLOOM_ADMIN -> {
                HatchloomAdmin admin = new HatchloomAdmin();
                admin.setUsername(request.getUsername());
                admin.setEmail(request.getEmail());
                admin.setPasswordHash(passwordHash);
                admin.setRole(roleType);
                yield admin;
            }
            case PARENT -> {
                Parent parent = new Parent();
                parent.setUsername(request.getUsername());
                parent.setEmail(request.getEmail());
                parent.setPasswordHash(passwordHash);
                parent.setRole(roleType);
                yield parent;
            }
        };
    }

    private void createDefaultProfileForUser(User user) {
        UserProfile profile = null;

        if (user instanceof Student || user instanceof SchoolTeacher) {
            profile = new AcademicProfile();
            ((AcademicProfile) profile).setGradeLevel("Not Set");
            ((AcademicProfile) profile).setSpecialization("Not Set");
        } else if (user instanceof SchoolAdmin || user instanceof HatchloomAdmin || user instanceof HatchloomTeacher) {
            profile = new ProfessionalProfile();
            ((ProfessionalProfile) profile).setJobTitle("Not Set");
            ((ProfessionalProfile) profile).setCompanyName("Not Set");
        } else if (user instanceof Parent) {
            profile = new ProfessionalProfile();
            ((ProfessionalProfile) profile).setJobTitle("Not Set");
            ((ProfessionalProfile) profile).setCompanyName("Not Set");
        }

        if (profile != null) {
            profile.setUser(user);
            profile.setBio("Bio");
            profile.setDescription("Description");
            user.setProfile(profile);
            userRepository.save(user);
        }
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
}

