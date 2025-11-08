package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public class Metamodel<C, P> {

    private final String name;
    private final List<ModelProperty<C, ?, P, ?>> properties = new ArrayList<>();

    public <A extends Anchor> Metamodel<C, P> anchorProperty(String name, Function<C, A> getter, BiConsumer<C, A> setter, Function<P, Optional<A>> patchGetter, BiConsumer<P, Optional<A>> patchSetter) {
        properties.add(new AnchorProperty<>(name, getter, setter, patchGetter, patchSetter));
        return this;
    }

    public <T> Metamodel<C, P> simpleProperty(String name, Function<C, T> getter, @Nullable BiConsumer<C, T> setter, Function<P, Optional<T>> patchGetter, BiConsumer<P, Optional<T>> patchSetter) {
        return simpleProperty(name, getter, setter, patchGetter, patchSetter, null);
    }

    public <T> Metamodel<C, P> simpleProperty(String name, Function<C, T> getter, @Nullable BiConsumer<C, T> setter, Function<P, Optional<T>> patchGetter, BiConsumer<P, Optional<T>> patchSetter, @Nullable T defaultValue) {
        properties.add(new SimpleProperty<>(name, getter, setter, patchGetter, patchSetter, defaultValue));
        return this;
    }

    public <U extends MeasurementUnit> Metamodel<C, P> enteredValueProperty(String name, Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter, Function<P, Optional<EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Optional<EnteredValuePatch<U>>> patchSetter) {
        return enteredValueProperty(name, getter, setter, patchGetter, patchSetter, null);
    }

    public <U extends MeasurementUnit> Metamodel<C, P> enteredValueProperty(String name, Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter, Function<P, Optional<EnteredValuePatch<U>>> patchGetter, BiConsumer<P, Optional<EnteredValuePatch<U>>> patchSetter, @Nullable EnteredValue<U> defaultValue) {
        properties.add(new EnteredValueProperty<>(name, getter, setter, patchGetter, patchSetter, defaultValue));
        return this;
    }

    public <I, IP> Metamodel<C, P> listProperty(String name, Function<C, List<I>> getter, BiConsumer<C, List<I>> setter, Function<P, Optional<Map<Integer, IP>>> patchGetter, BiConsumer<P, Optional<Map<Integer, IP>>> patchSetter, Metamodel<I, IP> childModel, ValueHandler<C, List<I>, Map<Integer, IP>> valueHandler) {
        properties.add(new ListProperty<>(name, getter, setter, patchGetter, patchSetter, childModel, valueHandler));
        return this;
    }

    public Metamodel<C, P> dictionaryProperty(String name, Function<C, DictionaryItemRef> getter, BiConsumer<C, DictionaryItemRef> setter, Function<P, Optional<DictionaryItemRef>> patchGetter, BiConsumer<P, Optional<DictionaryItemRef>> patchSetter) {
        properties.add(new DictionaryProperty<>(name, getter, setter, patchGetter, patchSetter));
        return this;
    }

    public Metamodel<C, P> dictionaryListProperty(String name, Function<C, List<DictionaryItemRef>> getter, BiConsumer<C, List<DictionaryItemRef>> setter, Function<P, Optional<List<DictionaryItemRef>>> patchGetter, BiConsumer<P, Optional<List<DictionaryItemRef>>> patchSetter) {
        properties.add(new DictionaryListProperty<>(name, getter, setter, patchGetter, patchSetter));
        return this;
    }

    public Metamodel<C, P> accept(Consumer<Metamodel<C, P>> block) {
        block.accept(this);
        return this;
    }
}
