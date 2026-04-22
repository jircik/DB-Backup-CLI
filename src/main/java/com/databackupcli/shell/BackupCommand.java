package com.databackupcli.shell;

import com.databackupcli.config.ProfileProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class BackupCommand {

    private static final Logger log = LoggerFactory.getLogger(BackupCommand.class);

    private final Job backupJob;
    private final JobLauncher syncJobLauncher;
    private final ProfileResolver profileResolver;

    public BackupCommand(Job backupJob,
                         @Qualifier("syncJobLauncher") JobLauncher syncJobLauncher,
                         ProfileResolver profileResolver) {
        this.backupJob       = backupJob;
        this.syncJobLauncher = syncJobLauncher;
        this.profileResolver = profileResolver;
    }

    @ShellMethod(key = "backup", value = "Run a database backup")
    public String backup(
            @ShellOption(value = "--profile",     defaultValue = ShellOption.NULL,
                         help = "Named profile from application.yml (dbbackup.profiles.<name>)") String profile,
            @ShellOption(value = "--db-type",     defaultValue = ShellOption.NULL,
                         help = "Database type: POSTGRES | MYSQL | MONGODB")                    String dbType,
            @ShellOption(value = "--host",        defaultValue = ShellOption.NULL,
                         help = "Database host")                                                String host,
            @ShellOption(value = "--port",        defaultValue = ShellOption.NULL,
                         help = "Database port")                                                String port,
            @ShellOption(value = "--database",    defaultValue = ShellOption.NULL,
                         help = "Database name")                                                String database,
            @ShellOption(value = "--username",    defaultValue = ShellOption.NULL,
                         help = "Database username")                                            String username,
            @ShellOption(value = "--password",    defaultValue = ShellOption.NULL,
                         help = "Database password")                                            String password,
            @ShellOption(value = "--backup-type", defaultValue = "FULL",
                         help = "Backup type: FULL | INCREMENTAL | DIFFERENTIAL")              String backupType,
            @ShellOption(value = "--storage",     defaultValue = "local",
                         help = "Storage backend: local | s3")                                  String storage
    ) {
        ProfileProperties resolved;
        try {
            resolved = profileResolver.resolve(profile, dbType, host, port, database, username, password);
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        }

        JobParameters params = new JobParametersBuilder()
                .addString("dbType",     resolved.getDbType().name())
                .addString("host",       resolved.getHost())
                .addString("port",       String.valueOf(resolved.getPort()))
                .addString("database",   resolved.getDatabase())
                .addString("username",   resolved.getUsername())
                .addString("password",   resolved.getPassword() != null ? resolved.getPassword() : "")
                .addString("backupType", backupType.toUpperCase())
                .addString("storage",    storage)
                .addLong("runId",        System.currentTimeMillis())   // ensures uniqueness per run
                .toJobParameters();

        log.info("Launching backup job for {}/{}", resolved.getHost(), resolved.getDatabase());

        try {
            JobExecution execution = syncJobLauncher.run(backupJob, params);
            String exitCode = execution.getExitStatus().getExitCode();

            if ("COMPLETED".equals(exitCode)) {
                String outputKey = execution.getExecutionContext().getString("outputKey", "unknown");
                return "Backup completed successfully.\nOutput: " + outputKey;
            } else {
                String desc = execution.getExitStatus().getExitDescription();
                return "Backup failed [" + exitCode + "]"
                        + (desc != null && !desc.isBlank() ? ":\n" + desc : "");
            }
        } catch (Exception e) {
            log.error("Backup job launch failed", e);
            return "Backup failed: " + e.getMessage();
        }
    }
}
