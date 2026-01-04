package com.epam.indigoeln.reaction.metamodel.property;

import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.*;
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

    public <T> void property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
            , Function<P, Patched<T>> patchGetter, BiConsumer<P, Patched<T>> patchSetter
    ) {
        property(name, getter, setter, patchGetter, patchSetter, DefaultDiffHandler.instance());
    }

    public <T, PT> void property(String name
            , Function<C, T> getter, @Nullable BiConsumer<C, T> setter
            , Function<P, Patched<PT>> patchGetter, BiConsumer<P, Patched<PT>> patchSetter
            , DiffHandler<T, PT> handler
    ) {
        properties.add(new SimpleProperty<>(name, getter, setter, patchGetter, patchSetter, handler));
    }

    public <U extends MeasurementUnit> void enteredValueProperty(String name
            , Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter
            , Function<P, Patched<EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Patched<EnteredValuePatch<U>>> patchSetter
    ) {
        enteredValueProperty(name, getter, setter, patchGetter, patchSetter, EnteredValueDiffHandler.instance());
    }

    public <U extends MeasurementUnit> void enteredValueProperty(String name
            , Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter
            , Function<P, Patched<EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Patched<EnteredValuePatch<U>>> patchSetter
            , EnteredValueDiffHandler<U> valueHandler
    ) {
        properties.add(new EnteredValueProperty<>(name, getter, setter, patchGetter, patchSetter, valueHandler));
    }

    public <I, A, IP> void listProperty(String name
            , Function<C, List<I>> getter, @Nullable BiConsumer<C, List<I>> setter
            , Function<P, Patched<ListPatch<IP>>> patchGetter, BiConsumer<P, Patched<ListPatch<IP>>> patchSetter
            , ListDiffHandler<I, A, IP> handler
    ) {
        properties.add(new ListProperty<C, I, A, P, IP>(name, getter, setter, patchGetter, patchSetter, handler));
    }

    public void accept(Consumer<Metamodel<C, P>> block) {
        block.accept(this);
    }
}
