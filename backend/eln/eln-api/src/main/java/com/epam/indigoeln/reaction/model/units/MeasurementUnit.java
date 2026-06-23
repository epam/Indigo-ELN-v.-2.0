package com.epam.indigoeln.reaction.model.units;

import one.util.streamex.StreamEx;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public sealed interface MeasurementUnit permits MolUnit, MolWeightUnit, VolumeUnit, WeightUnit, MolarityUnit, DensityUnit, NoUnit {

    String name();
    double getMultiplier();
    String getDisplayName();

    Map<String, MeasurementUnit> ALL_UNITS = Map.copyOf(
            StreamEx.of(MolUnit.values(), MolWeightUnit.values(), VolumeUnit.values(), WeightUnit.values(), MolarityUnit.values(), DensityUnit.values(), NoUnit.values())
                .flatMap(Stream::of)
                .toMap(Enum::name, Function.identity())
    );
}
