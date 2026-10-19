package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoUnit implements MeasurementUnit {

    NO_UNIT(1.0, "");

    private final double multiplier;
    private final String displayName;

    @Override
    public MeasurementUnit getBaseUnit() {
        return NO_UNIT;
    }
}
