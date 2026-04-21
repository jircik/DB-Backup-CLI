package com.databackupcli.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CompressionUtilTest {

    @Test
    void gzip_produces_gz_file_and_deletes_original(@TempDir Path tempDir) throws IOException {
        Path original = tempDir.resolve("dump.sql");
        Files.writeString(original, "SELECT 1; -- backup content that compresses well ".repeat(50));

        Path compressed = CompressionUtil.gzip(original);

        assertThat(compressed).exists();
        assertThat(compressed.getFileName().toString()).endsWith(".gz");
        assertThat(original).doesNotExist();
        assertThat(Files.size(compressed)).isGreaterThan(0);
    }

    @Test
    void gunzip_restores_original_content(@TempDir Path tempDir) throws IOException {
        String content = "SELECT 1; -- backup content ".repeat(100);
        Path original = tempDir.resolve("dump.sql");
        Files.writeString(original, content);

        Path compressed = CompressionUtil.gzip(original);
        Path restored = tempDir.resolve("restored.sql");

        CompressionUtil.gunzip(compressed, restored);

        assertThat(Files.readString(restored)).isEqualTo(content);
    }

    @Test
    void gzip_compresses_effectively(@TempDir Path tempDir) throws IOException {
        Path original = tempDir.resolve("dump.sql");
        String repetitiveContent = "SELECT 1; ".repeat(1000);
        Files.writeString(original, repetitiveContent);
        long originalSize = Files.size(original);

        Path compressed = CompressionUtil.gzip(original);

        assertThat(Files.size(compressed)).isLessThan(originalSize);
    }
}
