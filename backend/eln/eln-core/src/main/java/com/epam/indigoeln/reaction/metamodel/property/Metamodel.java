package com.epam.indigoeln.reaction.metamodel.property;

import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.patch.handler2.DefaultDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.DiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public class Metamodel<C, P> {

    private final String name;
    private final List<ModelProperty<C, ?, P, ?>> properties = new ArrayList<>();

    public <T> Metamodel<C, P> property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
            , Function<P, Patched<T>> patchGetter, BiConsumer<P, Patched<T>> patchSetter
    ) {
        return property(name, getter, setter, patchGetter, patchSetter, DefaultDiffHandler.instance(), null);
    }

    public <T, PT> Metamodel<C, P> property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
            , Function<P, Patched<PT>> patchGetter, BiConsumer<P, Patched<PT>> patchSetter
            , DiffHandler<T, PT> handler
    ) {
        return property(name, getter, setter, patchGetter, patchSetter, handler, null);
    }

    public <T> Metamodel<C, P> property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
            , Function<P, Patched<T>> patchGetter, BiConsumer<P, Patched<T>> patchSetter
            , @Nullable T defaultValue
    ) {
        return property(name, getter, setter, patchGetter, patchSetter, DefaultDiffHandler.instance(), defaultValue);
    }

    private <T, PT> Metamodel<C, P> property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
            , Function<P, Patched<PT>> patchGetter, BiConsumer<P, Patched<PT>> patchSetter
            , DiffHandler<T, PT> handler
            , @Nullable T defaultValue) {
        properties.add(new SimpleProperty<>(name, getter, setter, patchGetter, patchSetter, defaultValue, handler));
        return this;
    }

    public <U extends MeasurementUnit> Metamodel<C, P> enteredValueProperty(String name
            , Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter
            , Function<P, Patched<EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Patched<EnteredValuePatch<U>>> patchSetter
    ) {
        return enteredValueProperty(name, getter, setter, patchGetter, patchSetter, null);
    }

    public <U extends MeasurementUnit> Metamodel<C, P> enteredValueProperty(String name
            , Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter
            , Function<P, Patched<EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Patched<EnteredValuePatch<U>>> patchSetter
            , @Nullable EnteredValue<U> defaultValue
    ) {
        properties.add(new EnteredValueProperty<>(name, getter, setter, patchGetter, patchSetter, defaultValue));
        return this;
    }

    public void accept(Consumer<Metamodel<C, P>> block) {
        block.accept(this);
    }
}
