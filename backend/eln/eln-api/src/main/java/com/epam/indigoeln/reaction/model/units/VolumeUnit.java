package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VolumeUnit implements MeasurementUnit {

    ML(0.001, "mL"),
    L(1.0, "L");

    private final double multiplier;
    private final String displayName;
}
