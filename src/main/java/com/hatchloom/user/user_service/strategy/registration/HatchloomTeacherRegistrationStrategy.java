package com.hatchloom.user.user_service.strategy.registration;

import com.hatchloom.user.user_service.dto.RegisterRequest;
import com.hatchloom.user.user_service.model.HatchloomTeacher;
import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.User;
import org.springframework.stereotype.Component;

@Component
public class HatchloomTeacherRegistrationStrategy extends AbstractRoleRegistrationStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.HATCHLOOM_TEACHER;
    }

    @Override
    public boolean isValid(RegisterRequest request) {
        return true;
    }

    @Override
    public User createUser(RegisterRequest request, String passwordHash) {
        HatchloomTeacher teacher = new HatchloomTeacher();
        applyCommonFields(teacher, request, passwordHash);
        return teacher;
    }
}

