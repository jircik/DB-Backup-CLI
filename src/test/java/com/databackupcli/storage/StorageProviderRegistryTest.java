package com.databackupcli.storage;

import com.databackupcli.model.StorageException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageProviderRegistryTest {

    @Test
    void resolves_registered_provider_by_name() {
        StorageProvider mockProvider = mock(StorageProvider.class);
        when(mockProvider.name()).thenReturn("local");

        StorageProviderRegistry registry = new StorageProviderRegistry(List.of(mockProvider));

        assertThat(registry.get("local")).isSameAs(mockProvider);
    }

    @Test
    void throws_storage_exception_when_no_provider_for_name() {
        StorageProviderRegistry registry = new StorageProviderRegistry(List.of());

        assertThatThrownBy(() -> registry.get("s3"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("s3");
    }

    @Test
    void supports_multiple_providers() {
        StorageProvider localProvider = mock(StorageProvider.class);
        when(localProvider.name()).thenReturn("local");

        StorageProvider s3Provider = mock(StorageProvider.class);
        when(s3Provider.name()).thenReturn("s3");

        StorageProviderRegistry registry = new StorageProviderRegistry(List.of(localProvider, s3Provider));

        assertThat(registry.get("local")).isSameAs(localProvider);
        assertThat(registry.get("s3")).isSameAs(s3Provider);
    }
}
