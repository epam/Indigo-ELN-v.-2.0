package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record AnchorProperty<C, A extends Anchor, P> (
        String name,
        Function<C, A> getter,
        BiConsumer<C, A> setter,
        Function<P, Optional<A>> patchGetter,
        BiConsumer<P, Optional<A>> patchSetter
) implements ModelProperty<C, A, P, A> {

    @Override
    public ValueHandler<C, A, A> valueHandler() {
        return Handlers.defaultHandler();
    }
}
