package com.epam.indigoeln.reaction.model.outputsample;

import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class MeltingPoint {

    @Nullable
    private Double lower;

    @Nullable
    private Double upper;

    @Nullable
    private String comments;
}
