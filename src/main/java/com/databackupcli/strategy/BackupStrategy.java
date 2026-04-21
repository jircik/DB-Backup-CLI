package com.databackupcli.strategy;

import com.databackupcli.config.ProfileProperties;
import com.databackupcli.model.BackupException;
import com.databackupcli.model.BackupType;
import com.databackupcli.model.DbType;

import java.nio.file.Path;

public interface BackupStrategy {
    DbType supports();

    void testConnection(ProfileProperties profile) throws BackupException;

    Path dump(ProfileProperties profile, BackupType type, Path workDir) throws BackupException;

    void restore(Path backupFile, ProfileProperties profile) throws BackupException;
}
