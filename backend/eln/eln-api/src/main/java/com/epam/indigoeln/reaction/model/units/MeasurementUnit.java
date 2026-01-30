package com.epam.indigoeln.reaction.model.units;

import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public sealed interface MeasurementUnit permits MolUnit, MolWeightUnit, VolumeUnit, WeightUnit, MolarityUnit, DensityUnit, NoUnit {

    List<MeasurementUnit> UNITS = StreamEx.<MeasurementUnit[]>of(
            MolUnit.values(),
            MolWeightUnit.values(),
            VolumeUnit.values(),
            WeightUnit.values(),
            MolarityUnit.values(),
            DensityUnit.values(),
            NoUnit.values()
    ).flatMap(Arrays::stream).toList();

    Map<String, MeasurementUnit> UNIT_NAMES = StreamEx.of(UNITS)
            .toMap(MeasurementUnit::name, Function.identity());

    static MeasurementUnit getUnit(String name) {
        MeasurementUnit unit = MeasurementUnit.UNIT_NAMES.get(name);
        if (unit == null) {
            throw new IllegalArgumentException("Unknown unit: " + name);
        }
        return unit;
    }

    String name();
    double getMultiplier();
}
