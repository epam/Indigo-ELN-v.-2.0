package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.reaction.model.ComparisonOperator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
