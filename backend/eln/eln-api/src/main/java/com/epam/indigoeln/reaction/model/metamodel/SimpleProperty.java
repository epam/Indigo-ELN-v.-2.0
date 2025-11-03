package com.epam.indigoeln.reaction.model.metamodel;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record SimpleProperty<C, T>(
        String name,
        Function<C, T> getter,
        BiConsumer<C, T> setter
) implements ModelProperty<C, T> {
}
