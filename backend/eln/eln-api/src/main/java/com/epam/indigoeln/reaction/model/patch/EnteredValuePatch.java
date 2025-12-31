package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnteredValuePatch<U extends MeasurementUnit> {

    @Nullable
    private Patched<Double> value;
    @Nullable
    private Patched<U> unit;
    @Nullable
    private Patched<EnteredValueSource> source;
    @Nullable
    private Patched<Boolean> conflict;
}
