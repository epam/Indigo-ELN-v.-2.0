package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.reaction.model.units.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class MeasurementUtil {

    private static final Table<MeasurementUnit, MeasurementUnit, MeasurementUnit> MULTIPLY_TABLE = HashBasedTable.create();

    private static final Table<MeasurementUnit, MeasurementUnit, MeasurementUnit> DIVIDE_TABLE = HashBasedTable.create();

    static {
        MULTIPLY_TABLE.put(MolWeightUnit.G_PER_MOL, MolUnit.MOL, WeightUnit.KG);
        MULTIPLY_TABLE.put(MolWeightUnit.G_PER_MOL, MolUnit.MMOL, WeightUnit.G);
        MULTIPLY_TABLE.put(MolWeightUnit.G_PER_MOL, MolUnit.UMOL, WeightUnit.MG);
        MULTIPLY_TABLE.put(DensityUnit.G_ML, VolumeUnit.ML, WeightUnit.G);
        MULTIPLY_TABLE.put(DensityUnit.G_ML, VolumeUnit.L, WeightUnit.KG);
        MULTIPLY_TABLE.put(MolarityUnit.M, VolumeUnit.L, MolUnit.MOL);
        MULTIPLY_TABLE.put(MolarityUnit.MM, VolumeUnit.L, MolUnit.MMOL);
        MULTIPLY_TABLE.put(MolarityUnit.M, VolumeUnit.ML, MolUnit.MOL);
        MULTIPLY_TABLE.put(MolarityUnit.MM, VolumeUnit.ML, MolUnit.MMOL);

        DIVIDE_TABLE.put(WeightUnit.G, MolUnit.MOL, MolWeightUnit.G_PER_MOL);
        DIVIDE_TABLE.put(WeightUnit.G, MolUnit.MMOL, MolWeightUnit.G_PER_MOL);
        DIVIDE_TABLE.put(WeightUnit.G, MolUnit.UMOL, MolWeightUnit.G_PER_MOL);
        DIVIDE_TABLE.put(WeightUnit.MG, MolUnit.MOL, MolWeightUnit.G_PER_MOL);
        DIVIDE_TABLE.put(WeightUnit.MG, MolUnit.MMOL, MolWeightUnit.G_PER_MOL);
        DIVIDE_TABLE.put(WeightUnit.MG, MolUnit.UMOL, MolWeightUnit.G_PER_MOL);
        DIVIDE_TABLE.put(WeightUnit.KG, MolUnit.MOL, MolWeightUnit.G_PER_MOL);
        DIVIDE_TABLE.put(WeightUnit.KG, MolUnit.MMOL, MolWeightUnit.G_PER_MOL);
        DIVIDE_TABLE.put(WeightUnit.KG, MolUnit.UMOL, MolWeightUnit.G_PER_MOL);
        DIVIDE_TABLE.put(WeightUnit.G, MolWeightUnit.G_PER_MOL, MolUnit.MMOL);
        DIVIDE_TABLE.put(WeightUnit.MG, MolWeightUnit.G_PER_MOL, MolUnit.UMOL);
        DIVIDE_TABLE.put(WeightUnit.KG, MolWeightUnit.G_PER_MOL, MolUnit.MOL);
        DIVIDE_TABLE.put(WeightUnit.G, VolumeUnit.ML, DensityUnit.G_ML);
        DIVIDE_TABLE.put(WeightUnit.G, VolumeUnit.L, DensityUnit.G_ML);
        DIVIDE_TABLE.put(WeightUnit.MG, VolumeUnit.ML, DensityUnit.G_ML);
        DIVIDE_TABLE.put(WeightUnit.MG, VolumeUnit.L, DensityUnit.G_ML);
        DIVIDE_TABLE.put(WeightUnit.KG, VolumeUnit.ML, DensityUnit.G_ML);
        DIVIDE_TABLE.put(WeightUnit.KG, VolumeUnit.L, DensityUnit.G_ML);
        DIVIDE_TABLE.put(MolUnit.MOL, VolumeUnit.L, MolarityUnit.M);
        DIVIDE_TABLE.put(MolUnit.MMOL, VolumeUnit.L, MolarityUnit.MM);
        DIVIDE_TABLE.put(MolUnit.UMOL, VolumeUnit.L, MolarityUnit.MM);
        DIVIDE_TABLE.put(MolUnit.MOL, VolumeUnit.ML, MolarityUnit.M);
        DIVIDE_TABLE.put(MolUnit.MMOL, VolumeUnit.ML, MolarityUnit.MM);
        DIVIDE_TABLE.put(MolUnit.UMOL, VolumeUnit.ML, MolarityUnit.MM);
    }

    public static UnitAndMultiplier2 addOrSubtract(MeasurementUnit left, MeasurementUnit right) {
        double multiplierLeft = left.getMultiplier() / right.getMultiplier();
        return new UnitAndMultiplier2(right, multiplierLeft, 1.0);
    }

    public static UnitAndMultiplier multiply(MeasurementUnit left, MeasurementUnit right) {
        MeasurementUnit target = MULTIPLY_TABLE.get(left, right);
        if (target == null) {
            target = MULTIPLY_TABLE.get(right, left);
        }
        if (target == null && left instanceof NoUnit) {
            target = right;
        }
        if (target == null && right instanceof NoUnit) {
            target = left;
        }
        if (target == null) {
            throw new IllegalArgumentException("Cannot multiply units: " + left + " and " + right);
        }
        double multiplier = left.getMultiplier() * right.getMultiplier() / target.getMultiplier();
        return new UnitAndMultiplier(target, multiplier);
    }

    public static UnitAndMultiplier divide(MeasurementUnit left, MeasurementUnit right) {
        MeasurementUnit target = DIVIDE_TABLE.get(left, right);
        if (target == null && right instanceof NoUnit) {
            target = left;
        }
        if (target == null && left == right) {
            target = NoUnit.NO_UNIT;
        }
        if (target == null) {
            throw new IllegalArgumentException("Cannot divide units: " + left + " and " + right);
        }
        double multiplier = left.getMultiplier() / right.getMultiplier() / target.getMultiplier();
        return new UnitAndMultiplier(target, multiplier);
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
