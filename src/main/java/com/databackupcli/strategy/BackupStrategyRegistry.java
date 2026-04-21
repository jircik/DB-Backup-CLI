package com.databackupcli.strategy;

import com.databackupcli.model.BackupException;
import com.databackupcli.model.DbType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BackupStrategyRegistry {

    private final Map<DbType, BackupStrategy> strategies;

    public BackupStrategyRegistry(List<BackupStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(
                    BackupStrategy::supports,
                    Function.identity(),
                    (a, b) -> { throw new IllegalStateException(
                        "Duplicate BackupStrategy for DbType " + a.supports()
                        + ": " + a.getClass().getName() + " and " + b.getClass().getName()); }
                ));
    }

    public BackupStrategy get(DbType type) {
        return Optional.ofNullable(strategies.get(type))
                .orElseThrow(() -> new BackupException("No backup strategy registered for: " + type));
    }
}
