package com.epam.indigoeln.reaction.metamodel.property;

import com.epam.indigoeln.reaction.model.patch.handler2.DiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record SimpleProperty<C, T, P, PT>(
        String name,
        Function<C, T> getter,
        @Nullable BiConsumer<C, T> setter,
        Function<P, Patched<PT>> patchGetter,
        BiConsumer<P, Patched<PT>> patchSetter,
        @Nullable T defaultValue,
        DiffHandler<T, PT> valueHandler
) implements ModelProperty<C, T, P, PT> {
}
