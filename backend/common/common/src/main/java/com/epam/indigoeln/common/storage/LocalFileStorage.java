package com.epam.indigoeln.common.storage;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@DefaultBean
@ApplicationScoped
public class LocalFileStorage implements FileStorage {

    private final Path root;

    LocalFileStorage(
            @ConfigProperty(name = "eln.storage.local.root", defaultValue = ".") String root
    ) {
        this.root = Path.of(root);
    }

    @Override
    public List<String> list(String key) {
        Path dir = root.resolve(key);
        if (!Files.exists(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.map(f -> root.relativize(f).toString()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void put(String key, byte[] bytes) {
        try {
            Path path = root.resolve(key);
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public byte[] get(String key) {
        try {
            return Files.readAllBytes(root.resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
