package com.databackupcli.strategy.postgres;

import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.BackupException;
import com.databackupcli.model.BackupType;
import com.databackupcli.model.DbType;
import com.databackupcli.strategy.BackupStrategy;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class PostgresBackupStrategy implements BackupStrategy {

    @Override
    public DbType supports() {
        return DbType.POSTGRES;
    }

    @Override
    public void testConnection(ProfileProperties profile) throws BackupException {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                profile.getHost(), profile.getPort(), profile.getDatabase());
        try (Connection conn = DriverManager.getConnection(url, profile.getUsername(), profile.getPassword())) {
            if (!conn.isValid(5)) {
                throw new BackupException("PostgreSQL connection validation failed for: " + url);
            }
        } catch (SQLException e) {
            throw new BackupException("Cannot connect to PostgreSQL at " + url + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Path dump(ProfileProperties profile, BackupType type, Path workDir) throws BackupException {
        return new PgDumpCommand().execute(profile, workDir);
    }

    @Override
    public void restore(Path backupFile, ProfileProperties profile) throws BackupException {
        throw new BackupException("Restore not yet implemented for PostgreSQL");
    }
}
