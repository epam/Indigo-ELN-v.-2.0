package com.epam.indigoeln.common.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
public class ModelUtil {

    public <T> T firstNotNull(@Nullable T first, T second) {
        return first != null ? first : second;
    }

    public String formatUser(@Nullable String firstName, @Nullable String lastName, String username) {
        if (firstName == null && lastName == null) {
            return username;
        }
        StringBuilder s = new StringBuilder();
        if (firstName != null) {
            s.append(firstName);
        }
        if (lastName != null) {
            if (!s.isEmpty()) {
                s.append(' ');
            }
            s.append(lastName);
        }
        return s.toString();
    }

    public <T> boolean editProperty(JsonNullable<T> property, Consumer<T> consumer) {
        return editProperty(property, consumer, null, (Function<T, String>) null);
    }

    public <T> boolean editProperty(JsonNullable<T> property, Consumer<T> consumer, @Nullable List<String> summaryList, String propertyName) {
        return editProperty(property, consumer, summaryList, v -> propertyName + "=" + v);
    }

    public <T> boolean editProperty(JsonNullable<T> property, Consumer<T> consumer, @Nullable List<String> summaryList, @Nullable Function<T, String> summaryFn) {
        if (property.isPresent()) {
            T value = property.get();
            consumer.accept(value);
            if (summaryList != null) {
                String summary = summaryFn != null ? summaryFn.apply(value) : Objects.toString(value);
                summaryList.add(summary);
            }
            return true;
        }
        return false;
    }

    @SneakyThrows
    public byte[] loadResource(Class<?> klass, String resourceName) {
        try (InputStream is = loadResourceAsStream(klass, resourceName)) {
            return is.readAllBytes();
        }
    }

    @SneakyThrows
    public String loadResourceAsString(Class<?> klass, String resourceName) {
        try (InputStream is = loadResourceAsStream(klass, resourceName)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @SneakyThrows
    public InputStream loadResourceAsStream(Class<?> klass, String resourceName) {
        InputStream is = ModelUtil.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceName);
        }
        return is;
    }

    @Nullable
    public <T> List<T> emptyToNull(@Nullable List<T> list) {
        return list == null || list.isEmpty() ? null : list;
    }

    public boolean isNotEmpty(@Nullable Collection<?> list) {
        return list != null && !list.isEmpty();
    }

    public <T> void updateCollection(Collection<T> target, Collection<T> source) {
        Set<T> presentInTarget = target instanceof Set<T> ? (Set<T>) target : new HashSet<>(target);
        Set<T> deleted = new HashSet<>(target);
        for (T item : source) {
            if (!presentInTarget.contains(item)) {
                target.add(item);
            }
            deleted.remove(item);
        }
        target.removeAll(deleted);
    }

    @SneakyThrows
    public static <T> T useTempFile(String filename, byte[] bytes, Function<File, T> block) {
        Path directory = Files.createTempDirectory("eln");
        try {
            Path file = directory.resolve(filename);
            try {
                Files.write(file, bytes);
                return block.apply(file.toFile());
            } finally {
                Files.delete(file);
            }
        } finally {
            Files.delete(directory);
        }
    }

    public static <T> List<T> appendToList(List<T> list, T item) {
        return StreamEx.of(list).append(item).toImmutableList();
    }

    public static <T> List<T> removeFromList(List<T> list, T item) {
        return StreamEx.of(list).without(item).toImmutableList();
    }
}
