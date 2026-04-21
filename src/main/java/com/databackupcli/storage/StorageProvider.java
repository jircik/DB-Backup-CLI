package com.databackupcli.storage;

import com.databackupcli.model.StorageException;

import java.nio.file.Path;

public interface StorageProvider {
    String name();

    void store(Path localFile, String destinationKey) throws StorageException;

    Path retrieve(String key, Path localDest) throws StorageException;
}
