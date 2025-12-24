package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeightUnit implements MeasurementUnit {

    MG(0.001),
    G(1.0),
    KG(1000.0);

    private final double multiplier;
}
