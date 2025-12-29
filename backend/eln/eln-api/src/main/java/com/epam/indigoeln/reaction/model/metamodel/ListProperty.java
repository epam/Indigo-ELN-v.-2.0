package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.model.patch.handler.ValueHandler;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record ListProperty<C, I, P, IP>(
        String name,
        Function<C, List<I>> getter,
        BiConsumer<C, List<I>> setter,
        Function<P, Optional<ListPatch<Integer, IP>>> patchGetter,
        BiConsumer<P, Optional<ListPatch<Integer, IP>>> patchSetter,
        Metamodel<I, IP> childModel,
        ValueHandler<C, List<I>, ListPatch<Integer, IP>> valueHandler
) implements ModelProperty<C, List<I>, P, ListPatch<Integer, IP>> {
}
