package com.databackupcli.strategy.postgres;

import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.BackupException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

class PgDumpCommand {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    Path resolveOutputPath(ProfileProperties profile, Path workDir) {
        String filename = profile.getDatabase() + "-" + LocalDateTime.now().format(TIMESTAMP_FMT) + ".sql";
        return workDir.resolve(filename);
    }

    List<String> buildCommand(ProfileProperties profile, Path outputFile) {
        return List.of(
                "pg_dump",
                "-h", profile.getHost(),
                "-p", String.valueOf(profile.getPort()),
                "-U", profile.getUsername(),
                "-Fp",
                "-d", profile.getDatabase(),
                "-f", outputFile.toString()
        );
    }

    Path execute(ProfileProperties profile, Path workDir) throws BackupException {
        Path outputFile = resolveOutputPath(profile, workDir);
        List<String> cmd = buildCommand(profile, outputFile);

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.environment().put("PGPASSWORD", profile.getPassword());
            pb.redirectErrorStream(false);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            Process process = pb.start();

            String errorOutput = new String(process.getErrorStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new BackupException("pg_dump failed with exit code " + exitCode + ": " + errorOutput);
            }

            return outputFile;
        } catch (IOException e) {
            throw new BackupException("Failed to start pg_dump process", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackupException("pg_dump was interrupted", e);
        }
    }
}
