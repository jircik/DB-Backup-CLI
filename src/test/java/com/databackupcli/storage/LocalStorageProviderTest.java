package com.databackupcli.storage;

import com.databackupcli.config.BackupProperties;
import com.databackupcli.model.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageProviderTest {

    @Test
    void stores_file_at_destination_key_under_base_path(@TempDir Path base) throws IOException {
        LocalStorageProvider provider = providerWithBase(base);

        Path source = base.resolve("source.dump.gz");
        Files.writeString(source, "backup data");

        provider.store(source, "postgres/2026-01-01/backup.dump.gz");

        assertThat(base.resolve("postgres/2026-01-01/backup.dump.gz")).exists();
        assertThat(Files.readString(base.resolve("postgres/2026-01-01/backup.dump.gz")))
                .isEqualTo("backup data");
    }

    @Test
    void creates_parent_directories_automatically(@TempDir Path base) throws IOException {
        LocalStorageProvider provider = providerWithBase(base);

        Path source = base.resolve("source.dump.gz");
        Files.writeString(source, "data");

        provider.store(source, "deep/nested/dir/backup.dump.gz");

        assertThat(base.resolve("deep/nested/dir/backup.dump.gz")).exists();
    }

    @Test
    void retrieve_copies_stored_file_to_destination(@TempDir Path base) throws IOException {
        LocalStorageProvider provider = providerWithBase(base);

        Path source = base.resolve("source.dump.gz");
        Files.writeString(source, "backup data");
        provider.store(source, "postgres/backup.dump.gz");

        Path dest = base.resolve("retrieved.dump.gz");
        provider.retrieve("postgres/backup.dump.gz", dest);

        assertThat(Files.readString(dest)).isEqualTo("backup data");
    }

    @Test
    void retrieve_throws_when_key_not_found(@TempDir Path base) {
        LocalStorageProvider provider = providerWithBase(base);

        assertThatThrownBy(() -> provider.retrieve("nonexistent/file.gz", base.resolve("out.gz")))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("nonexistent/file.gz");
    }

    @Test
    void name_returns_local() {
        assertThat(providerWithBase(Path.of("/tmp")).name()).isEqualTo("local");
    }

    private LocalStorageProvider providerWithBase(Path base) {
        BackupProperties props = new BackupProperties();
        props.getStorage().getLocal().setPath(base.toString());
        return new LocalStorageProvider(props);
    }
}
