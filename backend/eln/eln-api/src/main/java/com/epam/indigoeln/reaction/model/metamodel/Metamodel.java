package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public class Metamodel<C> {

    private final String name;
    private final List<ModelProperty<C, ?>> properties = new ArrayList<>();

    public <A extends Anchor> Metamodel<C> anchorProperty(String name, Function<C, A> getter, BiConsumer<C, A> setter) {
        properties.add(new AnchorProperty<>(name, getter, setter));
        return this;
    }

    public <T> Metamodel<C> simpleProperty(String name, Function<C, T> getter, BiConsumer<C, T> setter) {
        properties.add(new SimpleProperty<>(name, getter, setter));
        return this;
    }

    public <U extends MeasurementUnit> Metamodel<C> enteredValueProperty(String name, Function<C, EnteredValue<U>> getter, BiConsumer<C, EnteredValue<U>> setter) {
        properties.add(new EnteredValueProperty<>(name, getter, setter));
        return this;
    }

    public <I> Metamodel<C> listProperty(String name, Function<C, List<I>> getter, BiConsumer<C, List<I>> setter, Metamodel<I> childModel) {
        properties.add(new ListProperty<>(name, getter, setter, childModel));
        return this;
    }

    public Metamodel<C> dictionaryProperty(String name, Function<C, DictionaryItemRef> getter, BiConsumer<C, DictionaryItemRef> setter) {
        properties.add(new DictionaryProperty<>(name, getter, setter));
        return this;
    }

    public Metamodel<C> dictionaryListProperty(String name, Function<C, List<DictionaryItemRef>> getter, BiConsumer<C, List<DictionaryItemRef>> setter) {
        properties.add(new DictionaryListProperty<>(name, getter, setter));
        return this;
    }

    public Metamodel<C> accept(Consumer<Metamodel<C>> block) {
        block.accept(this);
        return this;
    }
}
