package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.config.PatchedSerializers;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

@JsonSerialize(using = PatchedSerializers.Serializer.class)
@JsonDeserialize(using = PatchedSerializers.Deserializer.class)
public record Patched<T, P> (
    @Nullable T oldValue,
    @Nullable T newValue,
    @Nullable P updatedValue
) {

    public static <T, P> Patched<T, P> created(T newValue) {
        return new Patched<>(null, newValue, null);
    }

    public static <T, P> Patched<T, P> deleted(T oldValue) {
        return new Patched<>(oldValue, null, null);
    }

    public static <T, P> Patched<T, P> replaced(T oldValue, T newValue) {
        return new Patched<>(oldValue, newValue, null);
    }

    public static <T, P> Patched<T, P> updated(P value) {
        return new Patched<>(null, null, value);
    }

    public Patched {
        Preconditions.checkArgument(
                ((oldValue != null || newValue != null) && updatedValue == null) // created, deleted or replaced
                || (oldValue == null && newValue == null && updatedValue != null) // updated
        );
    }
}
