package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record EnteredValueProperty<C, U extends MeasurementUnit> (
        String name,
        Function<C, EnteredValue<U>> getter,
        BiConsumer<C, EnteredValue<U>> setter
) implements ModelProperty<C, EnteredValue<U>> {
}
