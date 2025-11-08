package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.patch.handler.DefaultValueHandler;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record SimpleListProperty<C, I, P>(
        String name,
        Function<C, List<I>> getter,
        @Nullable BiConsumer<C, List<I>> setter,
        Function<P, Optional<List<I>>> patchGetter,
        BiConsumer<P, Optional<List<I>>> patchSetter,
        @Nullable List<I> defaultValue
) implements ModelProperty<C, List<I>, P, List<I>> {

    @Override
    public ValueHandler<C, List<I>, List<I>> valueHandler() {
        return DefaultValueHandler.instance();
    }
}
