package com.databackupcli.shell;

import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.BackupException;
import com.databackupcli.strategy.BackupStrategyRegistry;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ConnectionTestCommand {

    private final BackupStrategyRegistry strategyRegistry;
    private final ProfileResolver profileResolver;

    public ConnectionTestCommand(BackupStrategyRegistry strategyRegistry,
                                 ProfileResolver profileResolver) {
        this.strategyRegistry = strategyRegistry;
        this.profileResolver  = profileResolver;
    }

    @ShellMethod(key = "test-connection", value = "Test database connectivity without running a backup")
    public String testConnection(
            @ShellOption(value = "--profile",  defaultValue = ShellOption.NULL,
                         help = "Named profile from application.yml (dbbackup.profiles.<name>)") String profile,
            @ShellOption(value = "--db-type",  defaultValue = ShellOption.NULL,
                         help = "Database type: POSTGRES | MYSQL | MONGODB")                    String dbType,
            @ShellOption(value = "--host",     defaultValue = ShellOption.NULL,
                         help = "Database host")                                                String host,
            @ShellOption(value = "--port",     defaultValue = ShellOption.NULL,
                         help = "Database port")                                                String port,
            @ShellOption(value = "--database", defaultValue = ShellOption.NULL,
                         help = "Database name")                                                String database,
            @ShellOption(value = "--username", defaultValue = ShellOption.NULL,
                         help = "Database username")                                            String username,
            @ShellOption(value = "--password", defaultValue = ShellOption.NULL,
                         help = "Database password")                                            String password
    ) {
        ProfileProperties resolved;
        try {
            resolved = profileResolver.resolve(profile, dbType, host, port, database, username, password);
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        }

        String target = resolved.getHost() + ":" + resolved.getPort() + "/" + resolved.getDatabase();

        try {
            strategyRegistry.get(resolved.getDbType()).testConnection(resolved);
            return "Connection successful → " + target + " [" + resolved.getDbType().getDisplayName() + "]";
        } catch (BackupException e) {
            return "Connection failed → " + target + "\nReason: " + e.getMessage();
        }
    }
}
