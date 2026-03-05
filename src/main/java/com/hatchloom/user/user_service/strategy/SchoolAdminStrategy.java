package com.hatchloom.user.user_service.strategy;

import com.hatchloom.user.user_service.model.IRolePermissionStrategy;
import com.hatchloom.user.user_service.model.RoleType;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SchoolAdminStrategy implements IRolePermissionStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.SCHOOL_ADMIN;
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of("CohortAnalytics", "ManageCohorts", "AddRemoveStudents", "ManageTeachers", "ViewSchoolData");
    }

    @Override
    public String getScope() {
        return "SINGLE_SCHOOL_LIMIT";
    }
}

