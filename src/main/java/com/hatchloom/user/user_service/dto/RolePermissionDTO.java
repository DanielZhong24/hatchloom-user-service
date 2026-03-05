package com.hatchloom.user.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionDTO {
    private String role;
    private Set<String> permissions;
    private String scope;
}

