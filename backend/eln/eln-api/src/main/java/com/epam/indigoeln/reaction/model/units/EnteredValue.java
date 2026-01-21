package com.epam.indigoeln.reaction.model.units;

import com.epam.indigoeln.reaction.util.MeasurementUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.Precision;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.epam.indigoeln.reaction.model.units.EnteredValueSource.DEFAULT;
import static com.google.common.base.MoreObjects.firstNonNull;

@Getter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class EnteredValue<U extends MeasurementUnit> {

    public static final EnteredValue<NoUnit> DEFAULT_ONE = defaultValue(1.0, NoUnit.NO_UNIT);
    public static final EnteredValue<MolUnit> ZERO_MOL = defaultValue(0.0, MolUnit.MOL);

    private final double value;
    private final U unit;
    private EnteredValueSource source;
    @Setter
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean conflict = false;

    @JsonCreator
    public EnteredValue(double value, U unit, EnteredValueSource source) {
        this.value = value;
        this.unit = unit;
        this.source = source;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> fixed(@Nullable Double value, U unit) {
        return value != null ? new EnteredValue<>(value, unit, EnteredValueSource.FIXED) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> userEntered(@Nullable Double value, @Nullable U unit, int revision) {
        return value != null && unit != null ? new EnteredValue<>(value, unit, EnteredValueSource.userEntered(revision)) : null;
    }

    public static <U extends MeasurementUnit> EnteredValue<U> userEntered(@Nullable Double value, U unit, double defaultValue, int revision) {
        return new EnteredValue<>(firstNonNull(value, defaultValue), unit, EnteredValueSource.userEntered(revision));
    }

    public static <U extends MeasurementUnit> EnteredValue<U> userEntered(@Nullable Double value, @Nullable U unit, double defaultValue, U defaultUnit, int revision) {
        return new EnteredValue<>(firstNonNull(value, defaultValue), firstNonNull(unit, defaultUnit), EnteredValueSource.userEntered(revision));
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> calculated(@Nullable Double value, U unit, EnteredValue<?> from1, EnteredValue<?> from2) {
        return value != null ? new EnteredValue<>(value, unit, EnteredValueSource.calculated(from1.source, from2.source)) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> defaultValue(@Nullable Double value, @Nullable U unit) {
        return value != null && unit != null ? new EnteredValue<>(value, unit, DEFAULT) : null;
    }

    public static <U extends MeasurementUnit> void prepareToRecalculate(EnteredValue<U> value, Consumer<EnteredValue<U>> setter, @Nullable EnteredValue<U> defaultValue) {
        doPrepareToRecalculate(value, setter, defaultValue);
    }

    private static <U extends MeasurementUnit> void doPrepareToRecalculate(@Nullable EnteredValue<U> value, Consumer<@Nullable EnteredValue<U>> setter, @Nullable EnteredValue<U> defaultValue) {
        if (value != null) {
            if (value.source.isCalculated()) {
                setter.accept(defaultValue); // clear calculated values
            }
            value.conflict = false;
        }
    }

    public static <R extends MeasurementUnit> EnteredValue<R> add(EnteredValue<R> left, EnteredValue<R> right) {
        MeasurementUtil.UnitAndMultiplier2 pair = MeasurementUtil.addOrSubtract(left.unit, right.unit);
        //noinspection unchecked
        return (EnteredValue<R>) calculated(left.value * pair.multiplier1() + right.value * pair.multiplier2(), pair.unit(), left, right);
    }

    public static <R extends MeasurementUnit> EnteredValue<R> subtract(EnteredValue<R> left, EnteredValue<R> right) {
        MeasurementUtil.UnitAndMultiplier2 pair = MeasurementUtil.addOrSubtract(left.unit, right.unit);
        //noinspection unchecked
        return (EnteredValue<R>) calculated(left.value * pair.multiplier1() - right.value * pair.multiplier2(), pair.unit(), left, right);
    }

    public static <A extends MeasurementUnit, B extends MeasurementUnit, R extends MeasurementUnit> EnteredValue<R> multiply(EnteredValue<A> left, EnteredValue<B> right) {
        MeasurementUtil.UnitAndMultiplier pair = MeasurementUtil.multiply(left.unit, right.unit);
        //noinspection unchecked
        return (EnteredValue<R>) calculated(left.value * right.value * pair.multiplier(), pair.unit(), left, right);
    }

    public static <U extends MeasurementUnit> EnteredValue<U> multiply(EnteredValue<U> self, double by) {
        return calculated(self.value * by, self.unit, self, self);
    }

    public static <R extends MeasurementUnit> EnteredValue<R> divide(EnteredValue<?> left, EnteredValue<?> right) {
        MeasurementUtil.UnitAndMultiplier pair = MeasurementUtil.divide(left.unit, right.unit);
        //noinspection unchecked
        return (EnteredValue<R>) calculated(left.value / right.value * pair.multiplier(), pair.unit(), left, right);
    }

    public static <U extends MeasurementUnit> EnteredValue<U> divide(EnteredValue<U> self, double by) {
        return calculated(self.value / by, self.unit, self, self);
    }

    public boolean valueEquals(EnteredValue<?> other) {
        Preconditions.checkArgument(unit.getClass().equals(other.unit.getClass()), "Non-comparable units: %s and %s", unit, other.unit);
        double thisValue = value * unit.getMultiplier();
        double otherValue = other.value * other.unit.getMultiplier();
        return Precision.equalsWithRelativeTolerance(thisValue, otherValue, 1e-6);
    }

    public <T extends MeasurementUnit> EnteredValue<T> cast() {
        //noinspection unchecked
        return (EnteredValue<T>) this;
    }

    @Override
    public String toString() {
        String str = source + ": " + Precision.round(value, 6) + " " + unit;
        if (conflict) {
            str += " [CONFLICT]";
        }
        return str;
    }
}
