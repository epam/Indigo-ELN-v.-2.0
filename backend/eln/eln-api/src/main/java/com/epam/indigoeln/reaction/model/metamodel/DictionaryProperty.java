package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record DictionaryProperty<C, P> (
        String name,
        Function<C, DictionaryItemRef> getter,
        BiConsumer<C, DictionaryItemRef> setter,
        Function<P, Optional<DictionaryItemRef>> patchGetter,
        BiConsumer<P, Optional<DictionaryItemRef>> patchSetter
) implements ModelProperty<C, DictionaryItemRef, P, DictionaryItemRef> {

    @Override
    public ValueHandler<C, DictionaryItemRef, DictionaryItemRef> valueHandler() {
        return Handlers.defaultHandler();
    }
}
