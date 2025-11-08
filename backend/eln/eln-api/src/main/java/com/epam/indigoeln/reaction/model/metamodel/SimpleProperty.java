package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.patch.handler.DefaultValueHandler;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record SimpleProperty<C, T, P>(
        String name,
        Function<C, T> getter,
        @Nullable BiConsumer<C, T> setter,
        Function<P, Optional<T>> patchGetter,
        BiConsumer<P, Optional<T>> patchSetter,
        @Nullable T defaultValue
) implements ModelProperty<C, T, P, T> {

    @Override
    public ValueHandler<C, T, T> valueHandler() {
        return DefaultValueHandler.instance();
    }
}
