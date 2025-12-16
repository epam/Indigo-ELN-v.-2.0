package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MolUnit implements MeasurementUnit {

    UMOL(0.000001),
    MMOL(0.001),
    MOL(1.0);

    private final double multiplier;
}
