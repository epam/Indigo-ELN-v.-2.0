package com.epam.indigoeln.reaction.model.outputsample;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@RequiredArgsConstructor
public class MeltingPoint {

    @Nullable
    private final Double lower;

    @Nullable
    private final Double upper;

    @Nullable
    private final String comments;
}
