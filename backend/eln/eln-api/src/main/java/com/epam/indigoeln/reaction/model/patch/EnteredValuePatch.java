package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnteredValuePatch<U extends MeasurementUnit> {

    @Nullable
    private Patched<String, String> value;
    @Nullable
    private Patched<U, U> unit;
    @Nullable
    private Patched<EnteredValueSource, EnteredValueSource> source;
    @Nullable
    private Patched<Boolean, Boolean> conflict;
}
