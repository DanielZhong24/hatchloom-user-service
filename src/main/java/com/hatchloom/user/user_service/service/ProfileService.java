package com.hatchloom.user.user_service.service;

import com.hatchloom.user.user_service.dto.ProfileDTO;
import com.hatchloom.user.user_service.dto.UpdateProfileRequest;
import com.hatchloom.user.user_service.model.*;
import com.hatchloom.user.user_service.repository.UserRepository;
import com.hatchloom.user.user_service.repository.UserProfileRepository;
import com.hatchloom.user.user_service.security.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SessionManager sessionManager;

    public ProfileDTO getProfile(String token, UUID userId) {
        try {
            User requestingUser = sessionManager.getUserFromSessionToken(token);

            if (requestingUser == null) {
                log.warn("Unauthorized profile access attempt");
                return null;
            }

            // Check authorization: user can only view their own profile unless admin
            if (!requestingUser.getId().equals(userId) &&
                !isAdmin(requestingUser.getRole())) {
                log.warn("Unauthorized profile access by user: {} for user: {}",
                        requestingUser.getId(), userId);
                return null;
            }

            User user = userRepository.findById(userId).orElse(null);

            if (user == null || !user.getActive()) {
                log.warn("User not found or inactive: {}", userId);
                return null;
            }

            return mapUserToProfileDTO(user);
        } catch (Exception e) {
            log.error("Error retrieving profile", e);
            return null;
        }
    }

    public ProfileDTO updateProfile(String token, UUID userId, UpdateProfileRequest request) {
        try {
            User requestingUser = sessionManager.getUserFromSessionToken(token);

            if (requestingUser == null) {
                log.warn("Unauthorized profile update attempt");
                return null;
            }

            // Users can only update their own profile
            if (!requestingUser.getId().equals(userId)) {
                log.warn("Unauthorized profile update by user: {} for user: {}",
                        requestingUser.getId(), userId);
                return null;
            }

            User user = userRepository.findById(userId).orElse(null);

            if (user == null || !user.getActive()) {
                log.warn("User not found or inactive: {}", userId);
                return null;
            }

            UserProfile profile = user.getProfile();

            if (profile != null) {
                if (request.getBio() != null) {
                    profile.setBio(request.getBio());
                }
                if (request.getDescription() != null) {
                    profile.setDescription(request.getDescription());
                }
                if (request.getProfilePictureUrl() != null) {
                    profile.setProfilePictureUrl(request.getProfilePictureUrl());
                }

                // Update profile-specific fields
                if (profile instanceof AcademicProfile) {
                    AcademicProfile academicProfile = (AcademicProfile) profile;
                    if (request.getGradeLevel() != null) {
                        academicProfile.setGradeLevel(request.getGradeLevel());
                    }
                    if (request.getSpecialization() != null) {
                        academicProfile.setSpecialization(request.getSpecialization());
                    }
                } else if (profile instanceof ProfessionalProfile) {
                    ProfessionalProfile professionalProfile = (ProfessionalProfile) profile;
                    if (request.getCompanyName() != null) {
                        professionalProfile.setCompanyName(request.getCompanyName());
                    }
                    if (request.getJobTitle() != null) {
                        professionalProfile.setJobTitle(request.getJobTitle());
                    }
                    if (request.getExpertise() != null) {
                        professionalProfile.setExpertise(request.getExpertise());
                    }
                }

                userProfileRepository.save(profile);
                log.info("Profile updated for user: {}", userId);
            }

            return mapUserToProfileDTO(user);
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return null;
        }
    }

    public Page<ProfileDTO> listProfiles(String token, Pageable pageable) {
        try {
            User requestingUser = sessionManager.getUserFromSessionToken(token);

            if (requestingUser == null) {
                log.warn("Unauthorized profiles list attempt");
                return Page.empty();
            }

            // Only admins can list all profiles
            if (!isAdmin(requestingUser.getRole())) {
                log.warn("Non-admin user attempted to list profiles: {}", requestingUser.getId());
                return Page.empty();
            }

            Page<User> users = userRepository.findAll(pageable);
            return users.map(this::mapUserToProfileDTO);
        } catch (Exception e) {
            log.error("Error listing profiles", e);
            return Page.empty();
        }
    }

    private ProfileDTO mapUserToProfileDTO(User user) {
        ProfileDTO.ProfileDTOBuilder builder = ProfileDTO.builder()
                .userId(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .createdAt(user.getCreatedAt().toString())
                .updatedAt(user.getUpdatedAt().toString());

        if (user.getProfile() != null) {
            UserProfile profile = user.getProfile();
            builder.bio(profile.getBio())
                   .description(profile.getDescription())
                   .profilePictureUrl(profile.getProfilePictureUrl());

            if (profile instanceof AcademicProfile) {
                AcademicProfile academicProfile = (AcademicProfile) profile;
                builder.gradeLevel(academicProfile.getGradeLevel())
                       .specialization(academicProfile.getSpecialization());
            } else if (profile instanceof ProfessionalProfile) {
                ProfessionalProfile professionalProfile = (ProfessionalProfile) profile;
                builder.companyName(professionalProfile.getCompanyName())
                       .jobTitle(professionalProfile.getJobTitle())
                       .expertise(professionalProfile.getExpertise());
            }
        }

        return builder.build();
    }

    private boolean isAdmin(RoleType role) {
        return role == RoleType.HATCHLOOM_ADMIN || role == RoleType.SCHOOL_ADMIN;
    }
}

