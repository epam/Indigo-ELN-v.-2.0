package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DensityUnit implements MeasurementUnit {

    G_ML(1.0, "g/mL");

    private final double multiplier;
    private final String displayName;
}
