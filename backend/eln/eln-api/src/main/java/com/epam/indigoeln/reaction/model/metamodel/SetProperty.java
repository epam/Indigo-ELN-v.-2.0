package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.model.patch.handler.ValueHandler;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record SetProperty<C, A extends Comparable<A>, I, P, IP>(
        String name,
        Function<C, Set<I>> getter,
        BiConsumer<C, Set<I>> setter,
        Function<P, Optional<ListPatch<A, IP>>> patchGetter,
        BiConsumer<P, Optional<ListPatch<A, IP>>> patchSetter,
        Metamodel<I, IP> childModel,
        ValueHandler<C, Set<I>, ListPatch<A, IP>> valueHandler
) implements ModelProperty<C, Set<I>, P, ListPatch<A, IP>> {
}
