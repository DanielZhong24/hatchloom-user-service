package com.hatchloom.user.user_service.model;

import java.util.Set;

public interface IRolePermissionStrategy {
    RoleType getRoleType();
    Set<String> getPermissions();
    String getScope();
}

