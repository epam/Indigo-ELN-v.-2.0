package com.epam.indigoeln.reaction.metamodel.property;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record ModelProperty<C, I extends @Nullable Object>(
        String name,
        Function<C, I> getter,
        @Nullable BiConsumer<C, I> setter,
        @Nullable I defaultValue,
        @Nullable Metamodel<?> childModel,
        boolean isEnteredValue
) {

    public static <C, T> ModelProperty<C, T> property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
    ) {
        return new ModelProperty<>(name, getter, setter, null, null, false);
    }

    public static <C, T> ModelProperty<C, T> property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
            , Metamodel<?> childModel
    ) {
        return new ModelProperty<>(name, getter, setter, null, childModel, false);
    }

    public static <C, U extends MeasurementUnit> ModelProperty<C, EnteredValue<U>> enteredValueProperty(String name
            , Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter
    ) {
        return new ModelProperty<>(name, getter, setter, null, null, true);
    }

    public static <C, U extends MeasurementUnit> ModelProperty<C, EnteredValue<U>> enteredValueProperty(String name
            , Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter
            , EnteredValue<U> defaultValue
    ) {
        return new ModelProperty<>(name, getter, setter, defaultValue, null, true);
    }

    public static <C, I> ModelProperty<C, List<I>> listProperty(String name
            , Function<C, List<I>> getter, @Nullable BiConsumer<C, List<I>> setter
            , Metamodel<I> childModel
    ) {
        return new ModelProperty<>(name, getter, setter, null, childModel, false);
    }

    public <C1, T1> ModelProperty<C1, T1> cast() {
        //noinspection unchecked
        return (ModelProperty<C1, T1>) this;
    }

    public I get(C container) {
        return getter().apply(container);
    }

    public void set(C container, I value) {
        BiConsumer<C, I> setter = setter();
        Preconditions.checkState(setter != null);
        setter.accept(container, value);
    }
}
