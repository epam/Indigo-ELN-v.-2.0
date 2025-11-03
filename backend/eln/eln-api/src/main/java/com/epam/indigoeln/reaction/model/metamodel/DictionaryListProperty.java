package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.eln.model.DictionaryItemRef;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record DictionaryListProperty<C> (
        String name,
        Function<C, List<DictionaryItemRef>> getter,
        BiConsumer<C, List<DictionaryItemRef>> setter
) implements ModelProperty<C, List<DictionaryItemRef>> {
}
