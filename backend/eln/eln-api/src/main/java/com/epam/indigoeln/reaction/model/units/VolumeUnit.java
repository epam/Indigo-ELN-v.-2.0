package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VolumeUnit implements MeasurementUnit {

    ML(0.001, "ml"),
    L(1.0, "l");

    private final double multiplier;
    private final String displayName;
}
