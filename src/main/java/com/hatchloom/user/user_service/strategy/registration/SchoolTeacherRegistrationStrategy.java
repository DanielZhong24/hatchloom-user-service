package com.hatchloom.user.user_service.strategy.registration;

import com.hatchloom.user.user_service.dto.RegisterRequest;
import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.SchoolTeacher;
import com.hatchloom.user.user_service.model.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SchoolTeacherRegistrationStrategy extends AbstractRoleRegistrationStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.SCHOOL_TEACHER;
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

        SchoolTeacher teacher = new SchoolTeacher();
        applyCommonFields(teacher, request, passwordHash);
        teacher.setSchoolId(schoolId);
        return teacher;
    }
}

