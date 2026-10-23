package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MolarityUnit implements MeasurementUnit {

    MM(1e-6, "mM"),
    M(1e-3, "M"),
    KM(1.0, "KM"); // artificial unit to work with other base units (mol and mL)

    private final double multiplier;
    private final String displayName;


    @Override
    public MeasurementUnit getBaseUnit() {
        return KM;
    }
}
