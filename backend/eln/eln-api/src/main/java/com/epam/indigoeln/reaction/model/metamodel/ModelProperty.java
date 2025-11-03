package com.epam.indigoeln.reaction.model.metamodel;

import java.util.function.BiConsumer;
import java.util.function.Function;

public sealed interface ModelProperty<C, T> permits AnchorProperty, SimpleProperty, EnteredValueProperty, ListProperty, DictionaryProperty, DictionaryListProperty {

    String name();

    Function<C, T> getter();

    BiConsumer<C, T> setter();

    default T get(C container) {
        return getter().apply(container);
    }

    default void set(C container, T value) {
        setter().accept(container, value);
    }
}
