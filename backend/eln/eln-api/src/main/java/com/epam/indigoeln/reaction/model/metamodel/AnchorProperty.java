package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.Anchor;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record AnchorProperty<C, A extends Anchor>(
        String name,
        Function<C, A> getter,
        BiConsumer<C, A> setter
) implements ModelProperty<C, A> {
}
