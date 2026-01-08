package com.epam.indigoeln.reaction.model.outputsample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeltingPoint {

    @Nullable
    private Double lower;

    @Nullable
    private Double upper;

    @Nullable
    private String comments;
}
