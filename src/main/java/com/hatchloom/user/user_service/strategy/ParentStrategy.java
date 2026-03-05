package com.hatchloom.user.user_service.strategy;

import com.hatchloom.user.user_service.model.IRolePermissionStrategy;
import com.hatchloom.user.user_service.model.RoleType;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ParentStrategy implements IRolePermissionStrategy {

    @Override
    public RoleType getRoleType() {
        return RoleType.PARENT;
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of("ViewChildWork", "ViewChildProgress", "ViewChildGrades", "UpdateOwnProfile", "ContactTeacher");
    }

    @Override
    public String getScope() {
        return "LINKED_CHILDREN_ONLY";
    }
}

