package com.databackupcli.batch;

import com.databackupcli.PostgresContainerBase;
import com.databackupcli.config.ProfileProperties;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("test")
class BackupJobIT extends PostgresContainerBase {

    @Autowired
    @Qualifier("syncJobLauncher")
    JobLauncher syncJobLauncher;

    @Autowired
    Job backupJob;

    // -----------------------------------------------------------------------
    // full job pipeline (requires pg_dump on PATH)
    // -----------------------------------------------------------------------

    @Test
    void fullBackupJob_completesSuccessfully() throws Exception {
        assumeTrue(isPgDumpAvailable(), "pg_dump not on PATH — skipping full job test");

        ProfileProperties profile = containerProfile();

        JobParameters params = new JobParametersBuilder()
                .addString("dbType",     profile.getDbType().name())
                .addString("host",       profile.getHost())
                .addString("port",       String.valueOf(profile.getPort()))
                .addString("database",   profile.getDatabase())
                .addString("username",   profile.getUsername())
                .addString("password",   profile.getPassword())
                .addString("backupType", "FULL")
                .addString("storage",    "local")
                .addLong("runId",        System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = syncJobLauncher.run(backupJob, params);

        assertEquals("COMPLETED", execution.getExitStatus().getExitCode(),
                "Job should complete successfully. Failures: " + execution.getAllFailureExceptions());

        String outputKey = execution.getExecutionContext().getString("outputKey", null);
        assertNotNull(outputKey, "outputKey should be stored in ExecutionContext");
        assertFalse(outputKey.isBlank(), "outputKey should not be blank");
    }

    // -----------------------------------------------------------------------
    // connection failure → job fails fast at validateConnectionStep
    // -----------------------------------------------------------------------

    @Test
    void backupJob_failsFastOnBadCredentials() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("dbType",     "POSTGRES")
                .addString("host",       POSTGRES.getHost())
                .addString("port",       String.valueOf(POSTGRES.getFirstMappedPort()))
                .addString("database",   POSTGRES.getDatabaseName())
                .addString("username",   "wrong-user")
                .addString("password",   "wrong-pass")
                .addString("backupType", "FULL")
                .addString("storage",    "local")
                .addLong("runId",        System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = syncJobLauncher.run(backupJob, params);

        assertEquals("FAILED", execution.getExitStatus().getExitCode(),
                "Job should fail when credentials are wrong");
    }
}
