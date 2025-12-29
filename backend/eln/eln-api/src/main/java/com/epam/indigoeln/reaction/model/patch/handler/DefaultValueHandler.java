package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalAssignedToNull")
public class DefaultValueHandler<C, T> implements ValueHandler<C, T, T> {

    @Nullable
    @Override
    public Optional<T> compare(Flag updated, @Nullable T a, @Nullable T b, @Nullable Optional<Integer> from) {
        if (Objects.equals(a, b)) {
            return null;
        }
        updated.set();
        if (b == null) {
            return Optional.empty();
        }
        return Optional.of(b);
    }

    @Nullable
    @Override
    public T apply(C container, @Nullable T value, @Nullable Optional<T> patch) {
        if (patch == null) {
            return value;
        }
        //noinspection OptionalIsPresent
        if (patch.isEmpty()) {
            return null;
        }
        return patch.get();
    }
}
