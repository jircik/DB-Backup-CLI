package com.databackupcli.shell;

import com.databackupcli.config.BackupProperties;
import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.DbType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves a {@link ProfileProperties} from a named config profile plus
 * optional CLI overrides. Either {@code profileName} or all of
 * (dbType, host, database, username) must be supplied.
 */
@Component
public class ProfileResolver {

    private final BackupProperties backupProperties;

    public ProfileResolver(BackupProperties backupProperties) {
        this.backupProperties = backupProperties;
    }

    public ProfileProperties resolve(String profileName,
                                     String dbType,
                                     String host,
                                     String port,
                                     String database,
                                     String username,
                                     String password) {
        ProfileProperties base;

        if (profileName != null) {
            ProfileProperties fromConfig = backupProperties.getProfiles().get(profileName);
            if (fromConfig == null) {
                throw new IllegalArgumentException(
                        "Profile '" + profileName + "' not found. Available: "
                        + backupProperties.getProfiles().keySet());
            }
            base = copy(fromConfig);
        } else {
            base = new ProfileProperties();
        }

        // Apply CLI overrides
        if (dbType != null)   base.setDbType(DbType.fromString(dbType));
        if (host != null)     base.setHost(host);
        if (port != null) {
            try {
                base.setPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port value: '" + port + "' — must be a number", e);
            }
        }
        if (database != null) base.setDatabase(database);
        if (username != null) base.setUsername(username);
        if (password != null) base.setPassword(password);

        validate(base);
        return base;
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private static void validate(ProfileProperties p) {
        List<String> missing = new ArrayList<>();
        if (p.getDbType() == null)                              missing.add("--db-type");
        if (p.getHost() == null || p.getHost().isBlank())      missing.add("--host");
        if (p.getPort() <= 0)                                   missing.add("--port");
        if (p.getDatabase() == null || p.getDatabase().isBlank()) missing.add("--database");
        if (p.getUsername() == null || p.getUsername().isBlank()) missing.add("--username");

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required parameters (or use --profile): " + missing);
        }
    }

    private static ProfileProperties copy(ProfileProperties src) {
        ProfileProperties c = new ProfileProperties();
        c.setDbType(src.getDbType());
        c.setHost(src.getHost());
        c.setPort(src.getPort());
        c.setDatabase(src.getDatabase());
        c.setUsername(src.getUsername());
        c.setPassword(src.getPassword());
        return c;
    }
}
