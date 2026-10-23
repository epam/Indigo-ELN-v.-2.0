package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.model.units.DensityUnit;
import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolWeightUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;

import static com.epam.indigoeln.common.model.units.DensityUnit.G_ML;
import static com.epam.indigoeln.common.model.units.MolUnit.MOL;
import static com.epam.indigoeln.common.model.units.MolWeightUnit.G_PER_MOL;
import static com.epam.indigoeln.common.model.units.MolarityUnit.M;
import static com.epam.indigoeln.common.model.units.NoUnit.NO_UNIT;
import static com.epam.indigoeln.common.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.common.model.units.WeightUnit.G;

@Slf4j
public class MeasurementUtil {

    private static final Table<Class<? extends MeasurementUnit>, Class<? extends MeasurementUnit>, MeasurementUnit> MULTIPLY_TABLE = HashBasedTable.create();

    private static final Table<Class<? extends MeasurementUnit>, Class<? extends MeasurementUnit>, MeasurementUnit> DIVIDE_TABLE = HashBasedTable.create();

    static {
        MULTIPLY_TABLE.put(MolarityUnit.class, VolumeUnit.class, MOL);
        MULTIPLY_TABLE.put(MolWeightUnit.class, MolUnit.class, G);
        MULTIPLY_TABLE.put(DensityUnit.class, VolumeUnit.class, G);
        MULTIPLY_TABLE.put(MolarityUnit.class, MolWeightUnit.class, G_ML);

        DIVIDE_TABLE.put(WeightUnit.class, MolWeightUnit.class, MOL);
        DIVIDE_TABLE.put(WeightUnit.class, DensityUnit.class, ML);
        DIVIDE_TABLE.put(WeightUnit.class, VolumeUnit.class, G_ML);
        DIVIDE_TABLE.put(WeightUnit.class, MolUnit.class, G_PER_MOL);

        DIVIDE_TABLE.put(MolUnit.class, MolarityUnit.class, ML);
        DIVIDE_TABLE.put(MolUnit.class, VolumeUnit.class, M);
        DIVIDE_TABLE.put(DensityUnit.class, MolarityUnit.class, G_PER_MOL);
        DIVIDE_TABLE.put(DensityUnit.class, MolWeightUnit.class, M);
    }

    public static MeasurementUnit multiply(MeasurementUnit left, MeasurementUnit right) {
        if (left instanceof NoUnit) {
            return right.getBaseUnit();
        }
        if (right instanceof NoUnit) {
            return left.getBaseUnit();
        }
        MeasurementUnit target = MULTIPLY_TABLE.get(left.getClass(), right.getClass());
        if (target == null) {
            target = MULTIPLY_TABLE.get(right.getClass(), left.getClass());
        }
        if (target == null) {
            throw new IllegalArgumentException("Cannot multiply units: " + left + " and " + right);
        }
        return target;
    }

    public static MeasurementUnit divide(MeasurementUnit left, MeasurementUnit right) {
        if (left == right) {
            return NO_UNIT;
        }
        if (right instanceof NoUnit) {
            return left.getBaseUnit();
        }
        if (left.getClass().equals(right.getClass())) {
            return NO_UNIT;
        }
        MeasurementUnit target = DIVIDE_TABLE.get(left.getClass(), right.getClass());
        if (target == null) {
            throw new IllegalArgumentException("Cannot divide units: " + left + " and " + right);
        }
        return target;
    }
}
