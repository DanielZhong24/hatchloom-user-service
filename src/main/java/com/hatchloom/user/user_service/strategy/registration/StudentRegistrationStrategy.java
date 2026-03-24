package com.hatchloom.user.user_service.strategy.registration;

import com.hatchloom.user.user_service.dto.RegisterRequest;
import com.hatchloom.user.user_service.model.RoleType;
import com.hatchloom.user.user_service.model.Student;
import com.hatchloom.user.user_service.model.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StudentRegistrationStrategy extends AbstractRoleRegistrationStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.STUDENT;
    }

    @Override
    public boolean isValid(RegisterRequest request) {
        return !isBlank(request.getSchoolId()) && request.getAge() != null && request.getAge() > 0;
    }

    @Override
    public User createUser(RegisterRequest request, String passwordHash) {
        UUID schoolId = tryParseUuid(request.getSchoolId());
        if (schoolId == null) {
            return null;
        }

        Student student = new Student();
        applyCommonFields(student, request, passwordHash);
        student.setSchoolId(schoolId);
        student.setAge(request.getAge());
        return student;
    }
}

