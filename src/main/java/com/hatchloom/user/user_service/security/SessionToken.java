package com.hatchloom.user.user_service.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionToken {
    private String accessToken;
    private String refreshToken;
}

