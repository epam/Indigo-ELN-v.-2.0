package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.model.units.DensityUnit;
import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolWeightUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.epam.indigoeln.reaction.model.UnitSource;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import one.util.streamex.StreamEx;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.epam.indigoeln.common.model.units.DensityUnit.G_ML;
import static com.epam.indigoeln.common.model.units.MolUnit.MOL;
import static com.epam.indigoeln.common.model.units.MolWeightUnit.G_PER_MOL;
import static com.epam.indigoeln.common.model.units.MolarityUnit.M;
import static com.epam.indigoeln.common.model.units.NoUnit.NO_UNIT;
import static com.epam.indigoeln.common.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.common.model.units.WeightUnit.G;

public class UnitUtils {

    public static Map<String, MeasurementUnit> ALL_UNITS = Map.copyOf(
            StreamEx.of(MolUnit.values(), MolWeightUnit.values(), VolumeUnit.values(), WeightUnit.values(), MolarityUnit.values(), DensityUnit.values(), NoUnit.values())
                    .flatMap(Stream::of)
                    .toMap(Enum::name, Function.identity())
    );

    private static final Table<MeasurementUnit, MeasurementUnit, BigDecimal> CONVERSION_TABLE;

    private static final MultiplicationRule[] RULES = {
            new MultiplicationRule(MOL, G_PER_MOL, G),
            new MultiplicationRule(G_PER_MOL, M, G_ML),
            new MultiplicationRule(ML, M, MOL),
            new MultiplicationRule(ML, G_ML, G)
    };

    private static final Table<Class<? extends MeasurementUnit>, Class<? extends MeasurementUnit>, MeasurementUnit> MULTIPLICATION_UNITS;
    private static final Table<Class<? extends MeasurementUnit>, Class<? extends MeasurementUnit>, MeasurementUnit> DIVISION_UNITS;

    static {
        ImmutableTable.Builder<MeasurementUnit, MeasurementUnit, BigDecimal> conversionBuilder = ImmutableTable.builder();
        for (MeasurementUnit[] values : Arrays.asList(MolUnit.values(), MolWeightUnit.values(), VolumeUnit.values(), WeightUnit.values(), MolarityUnit.values(), DensityUnit.values(), NoUnit.values())) {
            for (MeasurementUnit from : values) {
                for (MeasurementUnit to : values) {
                    if (from != to) {
                        //noinspection BigDecimalMethodWithoutRoundingCalled
                        conversionBuilder.put(from, to, from.getMultiplier().divide(to.getMultiplier()));
                    }
                }
            }
        }
        CONVERSION_TABLE = conversionBuilder.build();

        ImmutableTable.Builder<Class<? extends MeasurementUnit>, Class<? extends MeasurementUnit>, MeasurementUnit> multiplicationBuilder = ImmutableTable.builder();
        ImmutableTable.Builder<Class<? extends MeasurementUnit>, Class<? extends MeasurementUnit>, MeasurementUnit> divisionBuilder = ImmutableTable.builder();
        for (MultiplicationRule rule : RULES) {
            multiplicationBuilder.put(rule.a.getClass(), rule.b.getClass(), rule.r);
            multiplicationBuilder.put(rule.b.getClass(), rule.a.getClass(), rule.r);
            divisionBuilder.put(rule.r.getClass(), rule.a.getClass(), rule.b);
            divisionBuilder.put(rule.r.getClass(), rule.b.getClass(), rule.a);
        }
        MULTIPLICATION_UNITS = multiplicationBuilder.build();
        DIVISION_UNITS = divisionBuilder.build();
    }

    public static <R extends MeasurementUnit> R multiplicationUnit(MeasurementUnit left, MeasurementUnit right) {
        if (left instanceof NoUnit) {
            return right.getBaseUnit();
        }
        if (right instanceof NoUnit) {
            return left.getBaseUnit();
        }
        MeasurementUnit result = MULTIPLICATION_UNITS.get(left.getClass(), right.getClass());
        Preconditions.checkArgument(result != null, "Cannot multiply units: %s and %s",  left, right);
        //noinspection unchecked
        return (R) result;
    }

    public static <R extends MeasurementUnit> R divisionUnit(MeasurementUnit left, MeasurementUnit right) {
        if (left.getBaseUnit() == right.getBaseUnit()) {
            //noinspection unchecked
            return (R) NO_UNIT;
        }
        if (right instanceof NoUnit) {
            return left.getBaseUnit();
        }
        MeasurementUnit result = DIVISION_UNITS.get(left.getClass(), right.getClass());
        Preconditions.checkArgument(result != null, "Cannot divide units: %s and %s",  left, right);
        //noinspection unchecked
        return (R) result;
    }

    @SuppressWarnings("unchecked")
    public static <R extends MeasurementUnit> R getContextUnitByType(Class<?> type, UnitSource source) {
        if (type.equals(WeightUnit.class)) {
            return (R) source.getWeightUnit();
        } else if (type.equals(VolumeUnit.class)) {
            return (R) source.getVolumeUnit();
        } else if (type.equals(MolUnit.class)) {
            return (R) source.getMolUnit();
        } else if (type.equals(MolarityUnit.class)) {
            return (R) source.getMolarityUnit();
        } else if (type.equals(DensityUnit.class)) {
            return (R) G_ML;
        } else if (type.equals(MolWeightUnit.class)) {
            return (R) G_PER_MOL;
        } else if (type.equals(NoUnit.class)) {
            return (R) NO_UNIT;
        }
        throw new IllegalArgumentException("Unknown unit type: " + type);
    }

    public static <R extends MeasurementUnit> double convert(double value, R from, R to) {
        if (from == to) {
            return value;
        }
        BigDecimal factor = CONVERSION_TABLE.get(from, to);
        Preconditions.checkArgument(factor != null, "Inconvertible units: %s and %s", from, to);
        return factor.doubleValue() * value;
    }

    public static <R extends MeasurementUnit> BigDecimal convert(BigDecimal value, R from, R to) {
        if (from == to) {
            return value;
        }
        BigDecimal factor = CONVERSION_TABLE.get(from, to);
        Preconditions.checkArgument(factor != null, "Inconvertible units: %s and %s", from, to);
        return factor.multiply(value);
    }

    public static <R extends MeasurementUnit> double convertToBase(double value, R unit) {
        return convert(value, unit, unit.getBaseUnit());
    }

    public static <R extends MeasurementUnit> double convertFromBase(double value, R unit) {
        return convert(value, unit.getBaseUnit(), unit);
    }

    private record MultiplicationRule (
            MeasurementUnit a,
            MeasurementUnit b,
            MeasurementUnit r
    ) {}
}
