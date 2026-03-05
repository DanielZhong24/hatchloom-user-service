package com.hatchloom.user.user_service.strategy;

import com.hatchloom.user.user_service.model.IRolePermissionStrategy;
import com.hatchloom.user.user_service.model.RoleType;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class StudentStrategy implements IRolePermissionStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.STUDENT;
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of("ViewMyExperiences", "SubmitAssignments", "ViewMyGrades", "UpdateOwnProfile", "ViewMyProgress");
    }

    @Override
    public String getScope() {
        return "OWN_DATA_ONLY";
    }
}

