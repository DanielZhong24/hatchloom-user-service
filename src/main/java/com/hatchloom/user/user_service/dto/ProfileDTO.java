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
public class ProfileDTO {
    private String userId;
    private String username;
    private String email;
    private String role;
    private String bio;
    private String description;
    private String profilePictureUrl;

    // Academic profile fields (students and teachers)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String gradeLevel;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String specialization;

    // Student-specific metrics (only for STUDENT role)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lastActive;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer skillsCertified;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer explorerLevelXp;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer currentStreak;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer activeVentures;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer problemsTackled;

    // Professional profile fields (admins, parents)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String companyName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String jobTitle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String expertise;

    private String createdAt;
    private String updatedAt;
}
