package com.epam.indigoeln.reaction.metamodel.property;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.DiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record ListProperty<C, I, A, P, IP>(
        String name,
        Function<C, List<I>> getter,
        BiConsumer<C, List<I>> setter,
        Function<P, Patched<ListPatch<IP>>> patchGetter,
        BiConsumer<P, Patched<ListPatch<IP>>> patchSetter,
        ListDiffHandler<I, A, IP> valueHandler
) implements ModelProperty<C, List<I>, P, ListPatch<IP>> {
}
