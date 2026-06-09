package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.eln.model.SolventRef;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@RequiredArgsConstructor
public class ResidualSolvent {

    @NotNull
    private final SolventRef solvent;

    @NotNull
    private final Double eq;

    @Nullable
    private final String comment;
}
