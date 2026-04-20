package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MolarityUnit implements MeasurementUnit {

    MM(0.001, "mM"),
    M(1.0, "M");

    private final double multiplier;
    private final String displayName;
}
