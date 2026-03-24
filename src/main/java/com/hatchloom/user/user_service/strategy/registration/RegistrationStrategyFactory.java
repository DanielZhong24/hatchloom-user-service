package com.hatchloom.user.user_service.strategy.registration;

import com.hatchloom.user.user_service.model.RoleType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RegistrationStrategyFactory {

    private final Map<RoleType, RoleRegistrationStrategy> strategies;

    public RegistrationStrategyFactory(List<RoleRegistrationStrategy> strategyList) {
        this.strategies = new EnumMap<>(RoleType.class);
        for (RoleRegistrationStrategy strategy : strategyList) {
            this.strategies.put(strategy.getRoleType(), strategy);
        }
    }

    public RoleRegistrationStrategy getStrategy(RoleType roleType) {
        return strategies.get(roleType);
    }
}

