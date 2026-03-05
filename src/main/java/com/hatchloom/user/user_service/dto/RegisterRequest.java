package com.hatchloom.user.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String role;
    private String schoolId;  // For school-based roles
    private Integer age;      // For students
    private String companyName; // For entrepreneurs
}

