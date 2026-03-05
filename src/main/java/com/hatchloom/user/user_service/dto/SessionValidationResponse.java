package com.hatchloom.user.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionValidationResponse {
    private boolean valid;
    private String userId;
    private String role;
    private String message;
}

