package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.config.PatchedSerializers;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jspecify.annotations.Nullable;

@JsonSerialize(using = PatchedSerializers.Serializer.class)
@JsonDeserialize(using = PatchedSerializers.Deserializer.class)
public record Patched<T> (
    @Nullable T oldValue,
    @Nullable T value
) {

    public static <T> Patched<T> created(T newValue) {
        return new Patched<>(null, newValue);
    }

    public static <T> Patched<T> verbatim(T value) {
        return new Patched<>(null, value);
    }

    public static <T> Patched<T> deleted(T oldValue) {
        return new Patched<>(oldValue, null);
    }

    public static <T> Patched<T> updated(T oldValue, T newValue) {
        return new Patched<>(oldValue, newValue);
    }

    public Patched {
        if (oldValue == null && value == null) {
            throw new IllegalArgumentException("Both oldValue and value cannot be null");
        }
    }
}
