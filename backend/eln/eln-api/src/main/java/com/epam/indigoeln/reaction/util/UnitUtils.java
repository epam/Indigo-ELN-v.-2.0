package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.model.units.DensityUnit;
import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolWeightUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class UnitUtils {

    public static Map<String, MeasurementUnit> ALL_UNITS = Map.copyOf(
            StreamEx.of(MolUnit.values(), MolWeightUnit.values(), VolumeUnit.values(), WeightUnit.values(), MolarityUnit.values(), DensityUnit.values(), NoUnit.values())
                    .flatMap(Stream::of)
                    .toMap(Enum::name, Function.identity())
    );

    private static final ScopedValue<Units> UNITS = ScopedValue.newInstance();

    public static WeightUnit getWeightUnit() {
        return UNITS.get().weightUnit;
    }

    public static VolumeUnit getVolumeUnit() {
        return UNITS.get().volumeUnit;
    }

    public static MolUnit getMolUnit() {
        return UNITS.get().molUnit;
    }

    public static MolarityUnit getMolarityUnit() {
        return UNITS.get().molarityUnit;
    }

    public static <R extends MeasurementUnit> R getByUnit(R unit) {
        return (R) switch (unit) {
            case WeightUnit w -> getWeightUnit();
            case VolumeUnit v -> getVolumeUnit();
            case MolUnit m -> getMolUnit();
            case MolarityUnit m -> getMolarityUnit();
            case DensityUnit d -> DensityUnit.G_ML;
            case MolWeightUnit m -> MolWeightUnit.G_PER_MOL;
            case NoUnit n -> NoUnit.NO_UNIT;
            default -> throw new IllegalArgumentException();
        };
    }

    public static <T extends @Nullable Object, X extends Throwable> T callWithUnitsAndSignificantFigures(WeightUnit weightUnit, VolumeUnit volumeUnit, MolUnit molUnit, MolarityUnit molarityUnit, int significantFigures, ScopedValue.CallableOp<T, X> action) throws X {
        return ScopedValue.where(UNITS, new Units(weightUnit, volumeUnit, molUnit, molarityUnit)).call(() -> {
            return SignificantFiguresUtil.callWithSignificantFigures(significantFigures, action);
        });
    }

    public static <T extends @Nullable Object, X extends Throwable> T callWithUnitsAndSignificantFigures(ExperimentModel model, ScopedValue.CallableOp<T, X> action) throws X {
        return callWithUnitsAndSignificantFigures(model.getWeightUnit(), model.getVolumeUnit(), model.getMolUnit(), model.getMolarityUnit(), model.getSignificantFigures(), action);
    }

    private record Units (
            WeightUnit weightUnit,
            VolumeUnit volumeUnit,
            MolUnit molUnit,
            MolarityUnit molarityUnit
    ) {}
}
