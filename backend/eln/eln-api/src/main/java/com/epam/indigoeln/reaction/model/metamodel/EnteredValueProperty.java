package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.patch.handler.EnteredValueHandler;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record EnteredValueProperty<C, U extends MeasurementUnit, P> (
        String name,
        Function<C, EnteredValue<U>> getter,
        BiConsumer<C, EnteredValue<U>> setter,
        Function<P, Optional<EnteredValuePatch<U>>> patchGetter,
        BiConsumer<P, Optional<EnteredValuePatch<U>>> patchSetter,
        @Nullable EnteredValue<U> defaultValue
) implements ModelProperty<C, EnteredValue<U>, P, EnteredValuePatch<U>> {

    @Override
    public ValueHandler<C, EnteredValue<U>, EnteredValuePatch<U>> valueHandler() {
        return EnteredValueHandler.instance();
    }
}
