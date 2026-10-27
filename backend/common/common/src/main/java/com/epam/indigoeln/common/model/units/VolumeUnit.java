package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum VolumeUnit implements MeasurementUnit {

    ML(BigDecimal.ONE, "mL"),
    L(new BigDecimal("1e+3"), "L");

    private final BigDecimal multiplier;
    private final String displayName;


    @Override
    public MeasurementUnit getBaseUnit() {
        return ML;
    }
}
