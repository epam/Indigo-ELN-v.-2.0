package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum MolWeightUnit implements MeasurementUnit {

    G_PER_MOL(BigDecimal.ONE, "g/mol");

    private final BigDecimal multiplier;
    private final String displayName;


    @Override
    public MeasurementUnit getBaseUnit() {
        return G_PER_MOL;
    }
}
