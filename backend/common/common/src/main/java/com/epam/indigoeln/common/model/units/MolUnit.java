package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum MolUnit implements MeasurementUnit {

    UMOL(new BigDecimal("1e-6"), "μmol"),
    MMOL(new BigDecimal("1e-3"), "mmol"),
    MOL(BigDecimal.ONE, "mol");

    private final BigDecimal multiplier;
    private final String displayName;


    @Override
    public MeasurementUnit getBaseUnit() {
        return MOL;
    }
}
