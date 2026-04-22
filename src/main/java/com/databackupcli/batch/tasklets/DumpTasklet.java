package com.databackupcli.batch.tasklets;

import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.BackupType;
import com.databackupcli.model.DbType;
import com.databackupcli.strategy.BackupStrategyRegistry;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@StepScope
public class DumpTasklet implements Tasklet {

    private final BackupStrategyRegistry strategyRegistry;

    @Value("#{jobParameters['dbType']}")     private String dbType;
    @Value("#{jobParameters['host']}")       private String host;
    @Value("#{jobParameters['port']}")       private String port;
    @Value("#{jobParameters['database']}")   private String database;
    @Value("#{jobParameters['username']}")   private String username;
    @Value("#{jobParameters['password']}")   private String password;
    @Value("#{jobParameters['backupType']}") private String backupType;

    public DumpTasklet(BackupStrategyRegistry strategyRegistry) {
        this.strategyRegistry = strategyRegistry;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ProfileProperties profile = buildProfile();
        Path workDir = Files.createTempDirectory("dbbackup-");

        try {
            Path dumpFile = strategyRegistry.get(profile.getDbType())
                    .dump(profile, BackupType.valueOf(backupType), workDir);

            var executionContext = chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext();

            executionContext.putString("dumpFilePath", dumpFile.toString());
            executionContext.putString("workDir", workDir.toString());

        } catch (Exception e) {
            deleteQuietly(workDir);
            throw e;
        }

        return RepeatStatus.FINISHED;
    }

    private static void deleteQuietly(Path dir) {
        try {
            if (dir != null && java.nio.file.Files.exists(dir)) {
                java.nio.file.Files.walk(dir)
                        .sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> { try { java.nio.file.Files.delete(p); } catch (Exception ignored) {} });
            }
        } catch (Exception ignored) {}
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
