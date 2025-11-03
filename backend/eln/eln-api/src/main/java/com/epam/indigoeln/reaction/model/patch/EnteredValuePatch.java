package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class EnteredValuePatch<U extends MeasurementUnit> {

    @Nullable
    private Optional<Double> value;
    @Nullable
    private Optional<U> unit;
    @Nullable
    private Optional<EnteredValueSource> source;
    @Nullable
    private Optional<Boolean> conflict;
}
