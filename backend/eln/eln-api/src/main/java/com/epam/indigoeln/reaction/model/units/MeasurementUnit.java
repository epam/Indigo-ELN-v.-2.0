package com.epam.indigoeln.reaction.model.units;

public sealed interface MeasurementUnit permits MolUnit, MolWeightUnit, VolumeUnit, WeightUnit, MolarityUnit, DensityUnit, NoUnit {

    String name();
    double getMultiplier();
    String getDisplayName();
}
