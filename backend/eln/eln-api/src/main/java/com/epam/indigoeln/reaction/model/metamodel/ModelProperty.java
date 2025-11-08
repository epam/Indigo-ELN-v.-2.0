package com.epam.indigoeln.reaction.model.metamodel;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public sealed interface ModelProperty<C, T, P, PT> permits AnchorProperty, SimpleProperty, SimpleListProperty, EnteredValueProperty, ListProperty, DictionaryProperty, DictionaryListProperty {

    String name();

    Function<C, T> getter();

    @Nullable
    BiConsumer<C, T> setter();

    Function<P, Optional<PT>> patchGetter();

    BiConsumer<P, Optional<PT>> patchSetter();

    ValueHandler<C, T, PT> valueHandler();

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

    default Optional<PT> patchGet(P patch) {
        return patchGetter().apply(patch);
    }

    default void patchSet(P patch, Optional<PT> value) {
        patchSetter().accept(patch, value);
    }
}
