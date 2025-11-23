package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;

import static com.epam.indigoeln.reaction.model.units.DensityUnit.G_ML;
import static com.epam.indigoeln.reaction.model.units.MolUnit.*;
import static com.epam.indigoeln.reaction.model.units.MolWeightUnit.G_PER_MOL;
import static com.epam.indigoeln.reaction.model.units.MolarityUnit.M;
import static com.epam.indigoeln.reaction.model.units.MolarityUnit.MM;
import static com.epam.indigoeln.reaction.model.units.NoUnit.NO_UNIT;
import static com.epam.indigoeln.reaction.model.units.VolumeUnit.L;
import static com.epam.indigoeln.reaction.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.*;

@Slf4j
public class MeasurementUtil {

    private static final Table<MeasurementUnit, MeasurementUnit, UnitAndMultiplier> MULTIPLY_TABLE = HashBasedTable.create();

    private static final Table<MeasurementUnit, MeasurementUnit, UnitAndMultiplier> DIVIDE_TABLE = HashBasedTable.create();

    static {
        MULTIPLY_TABLE.put(M, L, new UnitAndMultiplier(MOL, 1));
        MULTIPLY_TABLE.put(MM, L, new UnitAndMultiplier(MMOL, 1));
        MULTIPLY_TABLE.put(M, ML, new UnitAndMultiplier(MMOL, 1));
        MULTIPLY_TABLE.put(MM, ML, new UnitAndMultiplier(UMOL, 1));
        MULTIPLY_TABLE.put(G_PER_MOL, MOL, new UnitAndMultiplier(G, 1));
        MULTIPLY_TABLE.put(G_PER_MOL, MMOL, new UnitAndMultiplier(MG, 1));
        MULTIPLY_TABLE.put(G_PER_MOL, UMOL, new UnitAndMultiplier(MG, 1e-3));
        MULTIPLY_TABLE.put(G_ML, ML, new UnitAndMultiplier(G, 1));
        MULTIPLY_TABLE.put(G_ML, L, new UnitAndMultiplier(KG, 1));

        DIVIDE_TABLE.put(MG, G_PER_MOL, new UnitAndMultiplier(MMOL, 1));
        DIVIDE_TABLE.put(G, G_PER_MOL, new UnitAndMultiplier(MOL, 1));
        DIVIDE_TABLE.put(KG, G_PER_MOL, new UnitAndMultiplier(MOL, 1000));
        DIVIDE_TABLE.put(MG, G_ML, new UnitAndMultiplier(ML, 1e-3));
        DIVIDE_TABLE.put(G, G_ML, new UnitAndMultiplier(ML, 1));
        DIVIDE_TABLE.put(KG, G_ML, new UnitAndMultiplier(L, 1));
        DIVIDE_TABLE.put(UMOL, MM, new UnitAndMultiplier(ML, 1));
        DIVIDE_TABLE.put(UMOL, M, new UnitAndMultiplier(ML, 1e-3));
        DIVIDE_TABLE.put(MMOL, MM, new UnitAndMultiplier(ML, 1000));
        DIVIDE_TABLE.put(MMOL, M, new UnitAndMultiplier(ML, 1));
        DIVIDE_TABLE.put(MOL, MM, new UnitAndMultiplier(L, 1000));
        DIVIDE_TABLE.put(MOL, M, new UnitAndMultiplier(L, 1));
    }

    public static UnitAndMultiplier2 addOrSubtract(MeasurementUnit left, MeasurementUnit right) {
        Preconditions.checkArgument(left.getClass().equals(right.getClass()), "Inconvertible units: %s and %s", left, right);
        double multiplierLeft = left.getMultiplier() / right.getMultiplier();
        return new UnitAndMultiplier2(right, multiplierLeft, 1.0);
    }

    public static UnitAndMultiplier multiply(MeasurementUnit left, MeasurementUnit right) {
        if (left instanceof NoUnit) {
            return new UnitAndMultiplier(right, 1.0);
        }
        if (right instanceof NoUnit) {
            return new UnitAndMultiplier(left, 1.0);
        }
        UnitAndMultiplier target = MULTIPLY_TABLE.get(left, right);
        if (target == null) {
            target = MULTIPLY_TABLE.get(right, left);
        }
        if (target == null) {
            throw new IllegalArgumentException("Cannot multiply units: " + left + " and " + right);
        }
        return target;
    }

    public static UnitAndMultiplier divide(MeasurementUnit left, MeasurementUnit right) {
        if (left == right) {
            return new UnitAndMultiplier(NO_UNIT, 1.0);
        }
        if (right instanceof NoUnit) {
            return new UnitAndMultiplier(left, 1.0);
        }
        if (left.getClass().equals(right.getClass())) {
            return new UnitAndMultiplier(NO_UNIT, left.getMultiplier() / right.getMultiplier());
        }
        UnitAndMultiplier target = DIVIDE_TABLE.get(left, right);
        if (target == null) {
            throw new IllegalArgumentException("Cannot divide units: " + left + " and " + right);
        }
        return target;
    }

    public static boolean nearlyEqual(double a, double b, double epsilon) {
        final double absA = Math.abs(a);
        final double absB = Math.abs(b);
        final double diff = Math.abs(a - b);

        if (a == b) {
            // shortcut, handles infinities
            return true;
        } else if (a == 0 || b == 0 || (absA + absB < Double.MIN_NORMAL)) {
            // a or b is zero or both are extremely close to it relative error is less meaningful here
            return diff < (epsilon * Double.MIN_NORMAL);
        } else {
            // use relative error
            return diff / Math.min((absA + absB), Double.MAX_VALUE) < epsilon;
        }
    }

    public record UnitAndMultiplier(MeasurementUnit unit, double multiplier) {}

    public record UnitAndMultiplier2(MeasurementUnit unit, double multiplier1, double multiplier2) {}
}
