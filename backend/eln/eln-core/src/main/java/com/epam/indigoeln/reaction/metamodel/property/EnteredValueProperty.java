package com.epam.indigoeln.reaction.metamodel.property;

import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.patch.handler2.DiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.EnteredValueDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record EnteredValueProperty<C, U extends MeasurementUnit, P> (
        String name,
        Function<C, EnteredValue<U>> getter,
        BiConsumer<C, EnteredValue<U>> setter,
        Function<P, Patched<EnteredValuePatch<U>>> patchGetter,
        BiConsumer<P, Patched<EnteredValuePatch<U>>> patchSetter,
        EnteredValueDiffHandler<U> valueHandler
) implements ModelProperty<C, EnteredValue<U>, P, EnteredValuePatch<U>> {
}
