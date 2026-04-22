package com.databackupcli.strategy.postgres;

import com.databackupcli.PostgresContainerBase;
import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.BackupException;
import com.databackupcli.model.BackupType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("test")
class PostgresBackupStrategyIT extends PostgresContainerBase {

    @Autowired
    PostgresBackupStrategy strategy;

    // -----------------------------------------------------------------------
    // testConnection
    // -----------------------------------------------------------------------

    @Test
    void testConnection_succeeds() {
        assertDoesNotThrow(() -> strategy.testConnection(containerProfile()));
    }

    @Test
    void testConnection_failsWithWrongPassword() {
        ProfileProperties bad = containerProfile();
        bad.setPassword("wrong-password");

        BackupException ex = assertThrows(BackupException.class,
                () -> strategy.testConnection(bad));

        assertNotNull(ex.getMessage());
    }

    @Test
    void testConnection_failsWithWrongHost() {
        ProfileProperties bad = containerProfile();
        bad.setHost("192.0.2.1"); // TEST-NET — guaranteed unreachable
        bad.setPort(5432);

        assertThrows(BackupException.class, () -> strategy.testConnection(bad));
    }

    // -----------------------------------------------------------------------
    // dump  (requires pg_dump on PATH)
    // -----------------------------------------------------------------------

    @Test
    void dump_createsNonEmptyFile(@TempDir Path workDir) throws Exception {
        assumeTrue(isPgDumpAvailable(), "pg_dump not on PATH — skipping dump test");

        Path dumpFile = strategy.dump(containerProfile(), BackupType.FULL, workDir);

        assertTrue(Files.exists(dumpFile), "Dump file should exist");
        assertTrue(Files.size(dumpFile) > 0, "Dump file should not be empty");
        assertTrue(dumpFile.getFileName().toString().endsWith(".sql"),
                "Dump file should have .sql extension");
    }
}
