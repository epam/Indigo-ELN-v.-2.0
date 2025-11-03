package com.epam.indigoeln.reaction.model.metamodel;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record ListProperty<C, I>(
        String name,
        Function<C, List<I>> getter,
        BiConsumer<C, List<I>> setter,
        Metamodel<I> childModel
) implements ModelProperty<C, List<I>> {
}
