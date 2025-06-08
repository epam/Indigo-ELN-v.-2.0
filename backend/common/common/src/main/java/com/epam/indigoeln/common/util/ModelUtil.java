package com.epam.indigoeln.common.util;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
}
