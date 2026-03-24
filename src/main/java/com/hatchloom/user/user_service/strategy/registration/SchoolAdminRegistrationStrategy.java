package com.hatchloom.user.user_service.strategy.registration;

import com.hatchloom.user.user_service.dto.RegisterRequest;
import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.SchoolAdmin;
import com.hatchloom.user.user_service.model.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SchoolAdminRegistrationStrategy extends AbstractRoleRegistrationStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.SCHOOL_ADMIN;
    }

    @Override
    public boolean isValid(RegisterRequest request) {
        return !isBlank(request.getSchoolId());
    }

    @Override
    public User createUser(RegisterRequest request, String passwordHash) {
        UUID schoolId = tryParseUuid(request.getSchoolId());
        if (schoolId == null) {
            return null;
        }

        SchoolAdmin admin = new SchoolAdmin();
        applyCommonFields(admin, request, passwordHash);
        admin.setSchoolId(schoolId);
        return admin;
    }
}

