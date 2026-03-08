package com.hatchloom.user.user_service.service;

import com.hatchloom.user.user_service.dto.ProfileDTO;
import com.hatchloom.user.user_service.dto.UpdateProfileRequest;
import com.hatchloom.user.user_service.model.AcademicProfile;
import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.Student;
import com.hatchloom.user.user_service.repository.UserProfileRepository;
import com.hatchloom.user.user_service.repository.UserRepository;
import com.hatchloom.user.user_service.security.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Profile Service Student Field Test Cases")
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private SessionManager sessionManager;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;
    private Student student;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        student = new Student();
        student.setId(userId);
        student.setUsername("student_1");
        student.setEmail("student1@example.com");
        student.setRole(RoleType.STUDENT);
        student.setActive(true);

        AcademicProfile profile = new AcademicProfile();
        profile.setUser(student);
        student.setProfile(profile);
    }

    @Test
    @DisplayName("Update profile persists student progression fields")
    void testUpdateProfileWithStudentProgressionFields() {
        // Arrange
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .gradeLevel("11")
                .specialization("Robotics")
                .skillsCertified(7)
                .explorerLevelXp(1330)
                .currentStreak(5)
                .activeVentures(3)
                .problemsTackled(42)
                .build();

        when(sessionManager.getUserFromSessionToken("token")).thenReturn(student);
        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(userProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProfileDTO response = profileService.updateProfile("token", userId, request);

        // Assert
        assertNotNull(response);
        assertEquals("11", response.getGradeLevel());
        assertEquals("Robotics", response.getSpecialization());
        assertEquals(7, response.getSkillsCertified());
        assertEquals(1330, response.getExplorerLevelXp());
        assertEquals(5, response.getCurrentStreak());
        assertEquals(3, response.getActiveVentures());
        assertEquals(42, response.getProblemsTackled());
        verify(userProfileRepository, times(1)).save(any());
    }
}

