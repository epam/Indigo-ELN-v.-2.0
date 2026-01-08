package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidualSolvent {

    @NotNull
    private DictionaryItemRef solvent;

    @NotNull
    private Double eq;

    @Nullable
    private String comment;
}
