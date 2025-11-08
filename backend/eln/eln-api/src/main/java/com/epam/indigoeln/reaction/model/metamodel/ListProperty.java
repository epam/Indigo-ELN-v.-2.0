package com.epam.indigoeln.reaction.model.metamodel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record ListProperty<C, I, P, IP>(
        String name,
        Function<C, List<I>> getter,
        BiConsumer<C, List<I>> setter,
        Function<P, Optional<Map<Integer, IP>>> patchGetter,
        BiConsumer<P, Optional<Map<Integer, IP>>> patchSetter,
        Metamodel<I, IP> childModel,
        ValueHandler<C, List<I>, Map<Integer, IP>> valueHandler
) implements ModelProperty<C, List<I>, P, Map<Integer, IP>> {
}
