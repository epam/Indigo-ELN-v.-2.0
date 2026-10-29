package com.epam.indigoeln.common.model.units;

import java.math.BigDecimal;

public sealed interface MeasurementUnit permits MolUnit, MolWeightUnit, VolumeUnit, WeightUnit, MolarityUnit, DensityUnit, NoUnit {

    String name();
    BigDecimal getMultiplier();
    String getDisplayName();

    <U extends MeasurementUnit> U getBaseUnit();

    default boolean isBase() {
        return this == getBaseUnit();
    }
}
