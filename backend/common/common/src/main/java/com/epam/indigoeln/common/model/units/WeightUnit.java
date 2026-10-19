package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeightUnit implements MeasurementUnit {

    MG(1e-3, "mg"),
    G(1.0, "g"),
    KG(1e+3, "kg");

    private final double multiplier;
    private final String displayName;


    @Override
    public MeasurementUnit getBaseUnit() {
        return G;
    }
}
