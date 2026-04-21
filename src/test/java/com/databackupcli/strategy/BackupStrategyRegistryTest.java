package com.databackupcli.strategy;

import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.BackupException;
import com.databackupcli.model.BackupType;
import com.databackupcli.model.DbType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BackupStrategyRegistryTest {

    @Test
    void resolves_registered_strategy_by_db_type() {
        BackupStrategy mockStrategy = mock(BackupStrategy.class);
        when(mockStrategy.supports()).thenReturn(DbType.POSTGRES);

        BackupStrategyRegistry registry = new BackupStrategyRegistry(List.of(mockStrategy));

        assertThat(registry.get(DbType.POSTGRES)).isSameAs(mockStrategy);
    }

    @Test
    void throws_backup_exception_when_no_strategy_for_type() {
        BackupStrategyRegistry registry = new BackupStrategyRegistry(List.of());

        assertThatThrownBy(() -> registry.get(DbType.POSTGRES))
                .isInstanceOf(BackupException.class)
                .hasMessageContaining("POSTGRES");
    }

    @Test
    void supports_multiple_strategies() {
        BackupStrategy pgStrategy = mock(BackupStrategy.class);
        when(pgStrategy.supports()).thenReturn(DbType.POSTGRES);

        BackupStrategy mysqlStrategy = mock(BackupStrategy.class);
        when(mysqlStrategy.supports()).thenReturn(DbType.MYSQL);

        BackupStrategyRegistry registry = new BackupStrategyRegistry(List.of(pgStrategy, mysqlStrategy));

        assertThat(registry.get(DbType.POSTGRES)).isSameAs(pgStrategy);
        assertThat(registry.get(DbType.MYSQL)).isSameAs(mysqlStrategy);
    }
}
