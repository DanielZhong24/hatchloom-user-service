package com.hatchloom.user.user_service.strategy;

import com.hatchloom.user.user_service.model.IRolePermissionStrategy;
import com.hatchloom.user.user_service.model.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry for role permission strategies.
 * Uses Spring's dependency injection to auto-discover all IRolePermissionStrategy implementations.
 * This approach follows the Open-Closed Principle - new strategies can be added without modifying this class.
 */
@Component
public class StrategyFactory {

    private final Map<RoleType, IRolePermissionStrategy> strategyMap;

    @Autowired
    public StrategyFactory(List<IRolePermissionStrategy> strategies) {
        // Auto-discover all strategy beans and map them by their role type
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        IRolePermissionStrategy::getRoleType,
                        Function.identity()
                ));
    }

    public IRolePermissionStrategy getStrategy(RoleType roleType) {
        IRolePermissionStrategy strategy = strategyMap.get(roleType);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for role: " + roleType);
        }
        return strategy;
    }
}

