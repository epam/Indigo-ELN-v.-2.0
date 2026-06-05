package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.reaction.model.ComparisonOperator;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@RequiredArgsConstructor
public class PurityCalculation {

    @NotNull
    private final PurityCalculationType type;

    @NotNull
    private final ComparisonOperator operator;

    @NotNull
    private final Double purity;

    @Nullable
    private final String comment;
}
