package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MolarityUnit implements MeasurementUnit {

    MM(0.001),
    M(1.0);

    private final double multiplier;
}
