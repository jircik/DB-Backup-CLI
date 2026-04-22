package com.databackupcli.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
public class BackupJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BackupJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Backup job starting",
                kv("jobId",       jobExecution.getJobId()),
                kv("jobName",     jobExecution.getJobInstance().getJobName()),
                kv("jobParameters", jobExecution.getJobParameters().toString()));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        java.time.LocalDateTime endTime = jobExecution.getEndTime() != null
                ? jobExecution.getEndTime()
                : java.time.LocalDateTime.now();
        Duration duration = Duration.between(jobExecution.getStartTime(), endTime);

        String exitCode = jobExecution.getExitStatus().getExitCode();
        String status   = jobExecution.getStatus().name();

        log.info("Backup job finished",
                kv("jobId",        jobExecution.getJobId()),
                kv("jobName",      jobExecution.getJobInstance().getJobName()),
                kv("batchStatus",  status),
                kv("exitCode",     exitCode),
                kv("durationMs",   duration.toMillis()));

        cleanupWorkDir(jobExecution);
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private void cleanupWorkDir(JobExecution jobExecution) {
        String workDirStr = jobExecution.getExecutionContext().getString("workDir", null);
        if (workDirStr == null) {
            return;
        }

        Path workDir = Path.of(workDirStr);
        if (!Files.exists(workDir)) {
            return;
        }

        try {
            Files.walk(workDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception ignored) {
                        }
                    });
            log.debug("Cleaned up work directory", kv("workDir", workDirStr));
        } catch (Exception e) {
            log.warn("Failed to clean up work directory",
                    kv("workDir", workDirStr),
                    kv("error", e.getMessage()));
        }
    }
}
