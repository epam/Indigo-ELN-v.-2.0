package com.epam.indigoeln.reaction.metamodel.property;

import com.epam.indigoeln.reaction.model.patch.handler2.DiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public sealed interface ModelProperty<C, T, P, PT> permits
        EnteredValueProperty,
        ListProperty,
        SimpleProperty
{

    String name();

    Function<C, T> getter();

    @Nullable
    BiConsumer<C, T> setter();

    Function<P, Patched<PT>> patchGetter();

    BiConsumer<P, Patched<PT>> patchSetter();

    DiffHandler<T, PT> valueHandler();

    @Nullable
    default T defaultValue() {
        return null;
    }

    default <C1, T1, P1, PT1, SELF extends ModelProperty<C1, T1, P1, PT1>> SELF cast() {
        //noinspection unchecked
        return (SELF) this;
    }

    default T get(C container) {
        return getter().apply(container);
    }

    default void set(C container, T value) {
        BiConsumer<C, T> setter = setter();
        Preconditions.checkState(setter != null);
        setter.accept(container, value);
    }

    default Patched<PT> patchGet(P patch) {
        return patchGetter().apply(patch);
    }

    default void patchSet(P patch, Patched<PT> value) {
        patchSetter().accept(patch, value);
    }
}
