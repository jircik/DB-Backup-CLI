package com.databackupcli.batch.tasklets;

import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.DbType;
import com.databackupcli.strategy.BackupStrategyRegistry;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ValidateConnectionTasklet implements Tasklet {

    private final BackupStrategyRegistry strategyRegistry;

    @Value("#{jobParameters['dbType']}")   private String dbType;
    @Value("#{jobParameters['host']}")     private String host;
    @Value("#{jobParameters['port']}")     private String port;
    @Value("#{jobParameters['database']}") private String database;
    @Value("#{jobParameters['username']}") private String username;
    @Value("#{jobParameters['password']}") private String password;

    public ValidateConnectionTasklet(BackupStrategyRegistry strategyRegistry) {
        this.strategyRegistry = strategyRegistry;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        ProfileProperties profile = buildProfile();
        strategyRegistry.get(profile.getDbType()).testConnection(profile);
        return RepeatStatus.FINISHED;
    }

    private ProfileProperties buildProfile() {
        ProfileProperties p = new ProfileProperties();
        p.setDbType(DbType.valueOf(dbType));
        p.setHost(host);
        try {
            p.setPort(Integer.parseInt(port));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port value: '" + port + "' — must be a number", e);
        }
        p.setDatabase(database);
        p.setUsername(username);
        p.setPassword(password);
        return p;
    }
}
