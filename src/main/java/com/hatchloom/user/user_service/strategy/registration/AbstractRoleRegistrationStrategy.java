package com.hatchloom.user.user_service.strategy.registration;

import com.hatchloom.user.user_service.dto.RegisterRequest;
import com.hatchloom.user.user_service.model.User;

import java.util.UUID;

public abstract class AbstractRoleRegistrationStrategy implements RoleRegistrationStrategy {

    protected void applyCommonFields(User user, RegisterRequest request, String passwordHash) {
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordHash);
        user.setRole(getRoleType());
    }

    protected boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    protected UUID tryParseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            return null;
        }
    }
}

