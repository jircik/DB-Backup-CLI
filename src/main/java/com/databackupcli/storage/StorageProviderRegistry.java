package com.databackupcli.storage;

import com.databackupcli.model.StorageException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StorageProviderRegistry {

    private final Map<String, StorageProvider> providers;

    public StorageProviderRegistry(List<StorageProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(
                    StorageProvider::name,
                    Function.identity(),
                    (a, b) -> { throw new IllegalStateException(
                        "Duplicate StorageProvider for name '" + a.name()
                        + "': " + a.getClass().getName() + " and " + b.getClass().getName()); }
                ));
    }

    public StorageProvider get(String name) {
        return Optional.ofNullable(providers.get(name))
                .orElseThrow(() -> new StorageException("No storage provider registered for: " + name));
    }
}
