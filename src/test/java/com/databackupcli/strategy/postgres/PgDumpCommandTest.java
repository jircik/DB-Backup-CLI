package com.databackupcli.strategy.postgres;

import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.DbType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PgDumpCommandTest {

    @Test
    void builds_pg_dump_command_with_correct_args(@TempDir Path workDir) {
        ProfileProperties profile = profileFor("db.example.com", 5432, "myapp", "backup_user");
        PgDumpCommand command = new PgDumpCommand();
        Path outputFile = workDir.resolve("backup.sql");

        List<String> cmd = command.buildCommand(profile, outputFile);

        assertThat(cmd).containsExactly(
                "pg_dump",
                "-h", "db.example.com",
                "-p", "5432",
                "-U", "backup_user",
                "-Fp",
                "-d", "myapp",
                "-f", outputFile.toString()
        );
    }

    @Test
    void output_filename_contains_database_name_and_timestamp(@TempDir Path workDir) {
        ProfileProperties profile = profileFor("localhost", 5432, "mydb", "user");
        PgDumpCommand command = new PgDumpCommand();

        Path outputFile = command.resolveOutputPath(profile, workDir);

        assertThat(outputFile.getFileName().toString()).startsWith("mydb-");
        assertThat(outputFile.getFileName().toString()).endsWith(".sql");
        assertThat(outputFile.getParent()).isEqualTo(workDir);
    }

    private ProfileProperties profileFor(String host, int port, String db, String username) {
        ProfileProperties p = new ProfileProperties();
        p.setDbType(DbType.POSTGRES);
        p.setHost(host);
        p.setPort(port);
        p.setDatabase(db);
        p.setUsername(username);
        p.setPassword("secret");
        return p;
    }
}
