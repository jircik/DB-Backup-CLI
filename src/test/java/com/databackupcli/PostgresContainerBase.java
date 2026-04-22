package com.databackupcli;

import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.DbType;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class that starts a single PostgreSQL container shared across all
 * tests in a subclass. The container is static so it is started once per
 * test class (not per test method).
 */
@Testcontainers
public abstract class PostgresContainerBase {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");

    /**
     * Build a {@link ProfileProperties} pointing at the running container.
     * Called per-test so each test gets a fresh copy it can mutate.
     */
    protected static ProfileProperties containerProfile() {
        ProfileProperties p = new ProfileProperties();
        p.setDbType(DbType.POSTGRES);
        p.setHost(POSTGRES.getHost());
        p.setPort(POSTGRES.getFirstMappedPort());
        p.setDatabase(POSTGRES.getDatabaseName());
        p.setUsername(POSTGRES.getUsername());
        p.setPassword(POSTGRES.getPassword());
        return p;
    }

    /** Returns true when {@code pg_dump} is available on the system PATH. */
    protected static boolean isPgDumpAvailable() {
        try {
            Process proc = new ProcessBuilder("pg_dump", "--version")
                    .redirectErrorStream(true)
                    .start();
            return proc.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
