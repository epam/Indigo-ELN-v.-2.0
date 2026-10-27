package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum NoUnit implements MeasurementUnit {

    NO_UNIT(BigDecimal.ONE, "");

    private final BigDecimal multiplier;
    private final String displayName;

    @Override
    public MeasurementUnit getBaseUnit() {
        return NO_UNIT;
    }
}
