package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum DensityUnit implements MeasurementUnit {

    G_ML(BigDecimal.ONE, "g/mL");

    private final BigDecimal multiplier;
    private final String displayName;


    @Override
    public MeasurementUnit getBaseUnit() {
        return G_ML;
    }
}
