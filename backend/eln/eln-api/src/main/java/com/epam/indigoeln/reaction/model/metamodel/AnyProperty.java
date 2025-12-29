package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.patch.handler.ValueHandler;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record AnyProperty<C, T, P, PT>(
        String name,
        Function<C, T> getter,
        @Nullable BiConsumer<C, T> setter,
        Function<P, Optional<PT>> patchGetter,
        BiConsumer<P, Optional<PT>> patchSetter,
        @Nullable T defaultValue,
        ValueHandler<C, T, PT> valueHandler
) implements ModelProperty<C, T, P, PT> {

}
