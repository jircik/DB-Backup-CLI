package com.databackupcli.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionUtil {

    private CompressionUtil() {}

    public static Path gzip(Path source) throws IOException {
        Path dest = source.resolveSibling(source.getFileName() + ".gz");
        try (InputStream in = Files.newInputStream(source);
             GZIPOutputStream out = new GZIPOutputStream(Files.newOutputStream(dest))) {
            in.transferTo(out);
        } catch (IOException e) {
            Files.deleteIfExists(dest);
            throw e;
        }
        Files.delete(source);
        return dest;
    }

    public static Path gunzip(Path source, Path dest) throws IOException {
        try (GZIPInputStream in = new GZIPInputStream(Files.newInputStream(source));
             OutputStream out = Files.newOutputStream(dest)) {
            in.transferTo(out);
        }
        return dest;
    }
}
