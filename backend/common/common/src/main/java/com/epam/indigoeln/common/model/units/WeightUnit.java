package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum WeightUnit implements MeasurementUnit {

    MG(new BigDecimal("1e-3"), "mg"),
    G(BigDecimal.ONE, "g"),
    KG(new BigDecimal("1e+3"), "kg");

    private final BigDecimal multiplier;
    private final String displayName;


    @Override
    public MeasurementUnit getBaseUnit() {
        return G;
    }
}
