package com.epam.indigoeln.common.storage;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@DefaultBean
@ApplicationScoped
public class LocalFileStorage implements FileStorage {

    @ConfigProperty(name = "eln.storage.local.root", defaultValue = ".")
    String root;

    @Override
    public void put(String key, byte[] bytes) {
        try {
            Path path = Path.of(root).resolve(key);
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public byte[] get(String key) {
        try {
            Path path = Path.of(root).resolve(key);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
