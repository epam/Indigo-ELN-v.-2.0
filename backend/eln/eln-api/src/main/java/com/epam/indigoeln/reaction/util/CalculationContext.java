package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.UnitSource;
import org.jspecify.annotations.Nullable;

public class CalculationContext {

    private static final ScopedValue<Context> VALUE = ScopedValue.newInstance();

    public static <T extends @Nullable Object, X extends Throwable> T call(int significantFigures, UnitSource units, ScopedValue.CallableOp<T, X> action) throws X {
        return ScopedValue.where(VALUE, new Context(significantFigures, units)).call(action);
    }

    public static <T extends @Nullable Object, X extends Throwable> T call(ExperimentModel model, ScopedValue.CallableOp<T, X> action) throws X {
        return ScopedValue.where(VALUE, new Context(model.getSignificantFigures(), model.getUnitSource())).call(action);
    }

    public static int getSignificantFigures() {
        return VALUE.get().significantFigures;
    }

    public static WeightUnit getWeightUnit() {
        return VALUE.get().units.getWeightUnit();
    }

    public static VolumeUnit getVolumeUnit() {
        return VALUE.get().units.getVolumeUnit();
    }

    public static MolUnit getMolUnit() {
        return VALUE.get().units.getMolUnit();
    }

    public static MolarityUnit getMolarityUnit() {
        return VALUE.get().units.getMolarityUnit();
    }

    private record Context (
            int significantFigures,
            UnitSource units
    ) {}
}
