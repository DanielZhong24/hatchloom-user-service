package com.hatchloom.user.user_service.strategy.registration;

import com.hatchloom.user.user_service.dto.RegisterRequest;
import com.hatchloom.user.user_service.model.Parent;
import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.User;
import org.springframework.stereotype.Component;

@Component
public class ParentRegistrationStrategy extends AbstractRoleRegistrationStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.PARENT;
    }

    @Override
    public boolean isValid(RegisterRequest request) {
        return true;
    }

    @Override
    public User createUser(RegisterRequest request, String passwordHash) {
        Parent parent = new Parent();
        applyCommonFields(parent, request, passwordHash);
        return parent;
    }
}

