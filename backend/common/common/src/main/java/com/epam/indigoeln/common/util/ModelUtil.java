package com.epam.indigoeln.common.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

@UtilityClass
public class ModelUtil {

    public <T> @NonNull T firstNotNull(@Nullable T first, T second) {
        return first != null ? first : second;
    }

    public String formatUser(@Nullable String firstName, @Nullable String lastName, String username) {
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
        return !s.isEmpty() ? s.toString() : username;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public <T> void editProperty(@Nullable Optional<T> property, Consumer<@Nullable T> consumer) {
        //noinspection OptionalAssignedToNull
        if (property != null) {
            consumer.accept(property.orElse(null));
        }
    }

    @SneakyThrows
    public byte[] loadResource(Class<?> klass, String resourceName) {
        try (InputStream is = loadResourceAsStream(klass, resourceName)) {
            return is.readAllBytes();
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
}
