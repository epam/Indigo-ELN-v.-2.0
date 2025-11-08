package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.patch.handler.DefaultValueHandler;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record DictionaryListProperty<C, P> (
        String name,
        Function<C, List<DictionaryItemRef>> getter,
        BiConsumer<C, List<DictionaryItemRef>> setter,
        Function<P, Optional<List<DictionaryItemRef>>> patchGetter,
        BiConsumer<P, Optional<List<DictionaryItemRef>>> patchSetter
) implements ModelProperty<C, List<DictionaryItemRef>, P, List<DictionaryItemRef>> {

    @Override
    public ValueHandler<C, List<DictionaryItemRef>, List<DictionaryItemRef>> valueHandler() {
        return DefaultValueHandler.instance();
    }
}
