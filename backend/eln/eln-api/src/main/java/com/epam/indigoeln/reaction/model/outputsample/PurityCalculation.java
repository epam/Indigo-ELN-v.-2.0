package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.reaction.model.ComparisonOperator;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class PurityCalculation {

    @NotNull
    private PurityCalculationType type;

    @NotNull
    private ComparisonOperator operator;

    @NotNull
    private Double purity;

    @Nullable
    private String comment;
}
