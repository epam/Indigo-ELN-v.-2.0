package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import org.jspecify.annotations.Nullable;

public record EnteredValueUndo<U extends MeasurementUnit>(
        @Nullable Double value,
        @Nullable U unit,
        @Nullable EnteredValueSource source
) {
}
