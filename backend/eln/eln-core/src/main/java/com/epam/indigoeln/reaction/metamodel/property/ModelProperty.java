package com.epam.indigoeln.reaction.metamodel.property;

import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.List;
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

    public static <C, T, P> ModelProperty<C, T, P, T> property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
            , Function<P, Patched<T, T>> patchGetter, BiConsumer<P, Patched<T, T>> patchSetter
    ) {
        return new ModelProperty<>(name, getter, setter, patchGetter, patchSetter, DefaultDiffHandler.instance());
    }

    public static <C, T, P, PT> ModelProperty<C, T, P, PT> property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
            , Function<P, Patched<T, PT>> patchGetter, BiConsumer<P, Patched<T, PT>> patchSetter
            , DiffHandler<T, PT> handler
    ) {
        return new ModelProperty<>(name, getter, setter, patchGetter, patchSetter, handler);
    }

    public static <C, U extends MeasurementUnit, P> ModelProperty<C, EnteredValue<U>, P, EnteredValuePatch<U>> enteredValueProperty(String name
            , Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter
            , Function<P, Patched<EnteredValue<U>, EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Patched<EnteredValue<U>, EnteredValuePatch<U>>> patchSetter
    ) {
        return enteredValueProperty(name, getter, setter, patchGetter, patchSetter, EnteredValueDiffHandler.instance());
    }

    public static <C, U extends MeasurementUnit, P> ModelProperty<C, EnteredValue<U>, P, EnteredValuePatch<U>> enteredValueProperty(String name
            , Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter
            , Function<P, Patched<EnteredValue<U>, EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Patched<EnteredValue<U>, EnteredValuePatch<U>>> patchSetter
            , EnteredValueDiffHandler<U> valueHandler
    ) {
        return new ModelProperty<>(name, getter, setter, patchGetter, patchSetter, valueHandler);
    }

    public static <C, I, A, P, IP> ModelProperty<C, List<I>, P, ListPatch<I, IP>> listProperty(String name
            , Function<C, List<I>> getter, @Nullable BiConsumer<C, List<I>> setter
            , Function<P, Patched<List<I>, ListPatch<I, IP>>> patchGetter, BiConsumer<P, Patched<List<I>, ListPatch<I, IP>>> patchSetter
            , ListDiffHandler<I, A, IP> handler
    ) {
        return new ModelProperty<>(name, getter, setter, patchGetter, patchSetter, handler);
    }

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
