package com.databackupcli.storage;

import com.databackupcli.config.BackupProperties;
import com.databackupcli.model.StorageException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class LocalStorageProvider implements StorageProvider {

    private final Path basePath;

    public LocalStorageProvider(BackupProperties properties) {
        String rawPath = properties.getStorage().getLocal().getPath();
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("dbbackup.storage.local.path must not be empty");
        }
        this.basePath = Path.of(rawPath);
    }

    @Override
    public String name() {
        return "local";
    }

    @Override
    public void store(Path localFile, String destinationKey) throws StorageException {
        try {
            Path dest = basePath.resolve(destinationKey);
            Files.createDirectories(dest.getParent());
            Files.copy(localFile, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException(
                "Failed to store " + localFile + " to local path: " + destinationKey, e);
        }
    }

    @Override
    public Path retrieve(String key, Path localDest) throws StorageException {
        try {
            Path source = basePath.resolve(key);
            Files.copy(source, localDest, StandardCopyOption.REPLACE_EXISTING);
            return localDest;
        } catch (IOException e) {
            throw new StorageException("Failed to retrieve key from local storage: " + key, e);
        }
    }
}
