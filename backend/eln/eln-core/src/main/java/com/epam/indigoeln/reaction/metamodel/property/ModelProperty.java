package com.epam.indigoeln.reaction.metamodel.property;

import com.epam.indigoeln.reaction.model.patch.handler2.DiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record ModelProperty<C, I, P, IP>(
        String name,
        Function<C, I> getter,
        @Nullable BiConsumer<C, I> setter,
        Function<P, Patched<I, IP>> patchGetter,
        BiConsumer<P, Patched<I, IP>> patchSetter,
        DiffHandler<I, IP> valueHandler
) {

    public <C1, T1, P1, PT1> ModelProperty<C1, T1, P1, PT1> cast() {
        //noinspection unchecked
        return (ModelProperty<C1, T1, P1, PT1>) this;
    }

    public I get(C container) {
        return getter().apply(container);
    }

    public void set(C container, I value) {
        BiConsumer<C, I> setter = setter();
        Preconditions.checkState(setter != null);
        setter.accept(container, value);
    }

    public Patched<I, IP> patchGet(P patch) {
        return patchGetter().apply(patch);
    }

    public void patchSet(P patch, Patched<I, IP> value) {
        patchSetter().accept(patch, value);
    }
}
