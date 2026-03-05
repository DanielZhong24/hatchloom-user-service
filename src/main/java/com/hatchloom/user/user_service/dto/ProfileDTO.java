package com.hatchloom.user.user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDTO {
    private String userId;
    private String username;
    private String email;
    private String role;
    private String bio;
    private String description;
    private String profilePictureUrl;
    private String gradeLevel;     // For academic profiles
    private String specialization; // For academic profiles
    private String companyName;    // For professional profiles
    private String jobTitle;       // For professional profiles
    private String expertise;      // For professional profiles
    private String createdAt;
    private String updatedAt;
}

