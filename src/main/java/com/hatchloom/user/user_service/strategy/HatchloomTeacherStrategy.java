package com.hatchloom.user.user_service.strategy;

import com.hatchloom.user.user_service.model.IRolePermissionStrategy;
import com.hatchloom.user.user_service.model.RoleType;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HatchloomTeacherStrategy implements IRolePermissionStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.HATCHLOOM_TEACHER;
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of("CreateGlobalCourses", "ViewCourses", "UpdateOwnCourses");
    }

    @Override
    public String getScope() {
        return "CROSS_SCHOOL_GLOBAL";
    }
}

