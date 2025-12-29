package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;
import com.epam.indigoeln.reaction.model.patch.handler.ValueHandler;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class Metamodel<C, P> {

    @Setter
    private String name;
    private final List<ModelProperty<C, ?, P, ?>> properties = new ArrayList<>();

    public <T> Metamodel<C, P> property(String name, Function<C, T> getter, @Nullable BiConsumer<C, T> setter, Function<P, Optional<T>> patchGetter, BiConsumer<P, Optional<T>> patchSetter) {
        return property(name, getter, setter, patchGetter, patchSetter, null);
    }

    public <T> Metamodel<C, P> property(String name, Function<C, T> getter, @Nullable BiConsumer<C, T> setter, Function<P, Optional<T>> patchGetter, BiConsumer<P, Optional<T>> patchSetter, @Nullable T defaultValue) {
        return listProperty(name, getter, setter, patchGetter, patchSetter, defaultValue, Handlers.defaultHandler());
    }

    public <I, IP> Metamodel<C, P> listProperty(String name, Function<C, I> getter, BiConsumer<C, I> setter, Function<P, Optional<IP>> patchGetter, BiConsumer<P, Optional<IP>> patchSetter, I defaultValue, ValueHandler<C, I, IP> handler) {
        properties.add(new SimpleProperty<>(name, getter, setter, patchGetter, patchSetter, defaultValue, handler));
        return this;
    }

    public <U extends MeasurementUnit> Metamodel<C, P> enteredValueProperty(String name, Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter, Function<P, Optional<EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Optional<EnteredValuePatch<U>>> patchSetter) {
        return enteredValueProperty(name, getter, setter, patchGetter, patchSetter, null);
    }

    public <U extends MeasurementUnit> Metamodel<C, P> enteredValueProperty(String name, Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter, Function<P, Optional<EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Optional<EnteredValuePatch<U>>> patchSetter, @Nullable EnteredValue<U> defaultValue) {
        properties.add(new EnteredValueProperty<>(name, getter, setter, patchGetter, patchSetter, defaultValue));
        return this;
    }

    public <I, IP> Metamodel<C, P> modelListProperty(String name, Function<C, List<I>> getter, BiConsumer<C, List<I>> setter, Function<P, Optional<ListPatch<Integer, IP>>> patchGetter, BiConsumer<P, Optional<ListPatch<Integer, IP>>> patchSetter, Metamodel<I, IP> childModel, ValueHandler<C, List<I>, ListPatch<Integer, IP>> valueHandler) {
        properties.add(new ListProperty<>(name, getter, setter, patchGetter, patchSetter, childModel, valueHandler));
        return this;
    }

    public Metamodel<C, P> accept(Consumer<Metamodel<C, P>> block) {
        block.accept(this);
        return this;
    }
}
