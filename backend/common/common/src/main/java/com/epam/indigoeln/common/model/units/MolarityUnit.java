package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum MolarityUnit implements MeasurementUnit {

    MM(new BigDecimal("1e-6"), "mM"),
    M(new BigDecimal("1e-3"), "M"),
    KM(BigDecimal.ONE, "KM"); // artificial unit to work with other base units (mol and mL)

    private final BigDecimal multiplier;
    private final String displayName;

    @Override
    public MeasurementUnit getBaseUnit() {
        return KM;
    }
}
