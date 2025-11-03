package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.eln.model.DictionaryItemRef;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record DictionaryProperty<C> (
        String name,
        Function<C, DictionaryItemRef> getter,
        BiConsumer<C, DictionaryItemRef> setter
) implements ModelProperty<C, DictionaryItemRef> {
}
