package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeightUnit implements MeasurementUnit {

    MG(0.001, "mg"),
    G(1.0, "g"),
    KG(1000.0, "kg");

    private final double multiplier;
    private final String displayName;
}
