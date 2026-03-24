package com.hatchloom.user.user_service.strategy.registration;

import com.hatchloom.user.user_service.dto.RegisterRequest;
import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.User;

public interface RoleRegistrationStrategy {
    RoleType getRoleType();

    boolean isValid(RegisterRequest request);

    User createUser(RegisterRequest request, String passwordHash);
}

