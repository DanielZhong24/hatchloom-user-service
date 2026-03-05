package com.hatchloom.user.user_service.strategy;

import com.hatchloom.user.user_service.model.IRolePermissionStrategy;
import com.hatchloom.user.user_service.model.RoleType;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HatchloomAdminStrategy implements IRolePermissionStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.HATCHLOOM_ADMIN;
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of("ManageClients", "GlobalAnalytics", "ManageUsers", "ManageSchools");
    }

    @Override
    public String getScope() {
        return "UNRESTRICTED";
    }
}

