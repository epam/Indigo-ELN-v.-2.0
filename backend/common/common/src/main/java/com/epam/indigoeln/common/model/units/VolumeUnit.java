package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VolumeUnit implements MeasurementUnit {

    ML(1.0, "mL"),
    L(1e+3, "L");

    private final double multiplier;
    private final String displayName;


    @Override
    public MeasurementUnit getBaseUnit() {
        return ML;
    }
}
