package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class ResidualSolvent {

    @NotNull
    private DictionaryItemRef solvent;

    @NotNull
    private Double eq;

    @Nullable
    private String comment;
}
