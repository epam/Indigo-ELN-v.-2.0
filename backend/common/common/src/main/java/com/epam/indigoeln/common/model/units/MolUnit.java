package com.epam.indigoeln.common.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MolUnit implements MeasurementUnit {

    UMOL(1e-6, "μmol"),
    MMOL(1e-3, "mmol"),
    MOL(1.0, "mol");

    private final double multiplier;
    private final String displayName;


    @Override
    public MeasurementUnit getBaseUnit() {
        return MOL;
    }
}
