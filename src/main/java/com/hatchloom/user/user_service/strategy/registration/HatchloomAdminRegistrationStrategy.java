package com.hatchloom.user.user_service.strategy.registration;

import com.hatchloom.user.user_service.dto.RegisterRequest;
import com.hatchloom.user.user_service.model.HatchloomAdmin;
import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.User;
import org.springframework.stereotype.Component;

@Component
public class HatchloomAdminRegistrationStrategy extends AbstractRoleRegistrationStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.HATCHLOOM_ADMIN;
    }

    @Override
    public boolean isValid(RegisterRequest request) {
        return true;
    }

    @Override
    public User createUser(RegisterRequest request, String passwordHash) {
        HatchloomAdmin admin = new HatchloomAdmin();
        applyCommonFields(admin, request, passwordHash);
        return admin;
    }
}

