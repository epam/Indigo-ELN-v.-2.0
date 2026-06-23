package com.epam.indigoeln.reaction.model.units;

import com.epam.indigoeln.reaction.util.MeasurementUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.math3.util.Precision;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

import static com.epam.indigoeln.reaction.model.units.EnteredValueSource.DEFAULT;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.*;
import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode(of = {"stringValue", "unit", "source"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class EnteredValue<U extends MeasurementUnit> {

    public static final EnteredValue<NoUnit> DEFAULT_ONE = defaultValue(1.0, 1, NoUnit.NO_UNIT);
    public static final EnteredValue<NoUnit> DEFAULT_ONE_HUNDRED = defaultValue(100.0, 1, NoUnit.NO_UNIT);

    @Getter
    @JsonIgnore
    private final double value;

    @Getter
    private final U unit;

    @JsonIgnore
    private final int significantFigures; // used only to lazily format stringValue

    @Getter
    private final EnteredValueSource source;

    @Nullable
    private String stringValue;

    @JsonProperty("$overwritten")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean overwritten;

    @JsonCreator
    EnteredValue(String stringValue, U unit, EnteredValueSource source) {
        this(Double.parseDouble(stringValue), -1, stringValue, unit, source, false);
    }

    private EnteredValue(double value, int significantFigures, @Nullable String stringValue, U unit, EnteredValueSource source, boolean overwritten) {
        this.value = value;
        this.significantFigures = significantFigures;
        this.stringValue = stringValue;
        this.unit = unit;
        this.source = source;
        this.overwritten = overwritten;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> fixed(@Nullable Double value, int precision, U unit) {
        return value != null ? new EnteredValue<>(roundToSignificantFigures(value, precision), precision, null, unit, EnteredValueSource.FIXED, false) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> fixed(@Nullable BigDecimal value, U unit) {
        return value != null ? new EnteredValue<>(value.doubleValue(), -1, value.toString(), unit, EnteredValueSource.FIXED, false) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> userEntered(@Nullable String stringValue, @Nullable U unit, int revision) {
        return stringValue != null && unit != null ? new EnteredValue<>(Double.parseDouble(stringValue), -1, stringValue, unit, EnteredValueSource.userEntered(revision), false) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> calculated(@Nullable Double value, U unit) {
        return value != null ? new EnteredValue<>(value, getSignificantFigures(), null, unit, EnteredValueSource.CALCULATED, false) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> defaultValue(@Nullable Double value, int precision, @Nullable U unit) {
        return value != null && unit != null ? new EnteredValue<>(roundToSignificantFigures(value, precision), precision, null, unit, DEFAULT, false) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> defaultValue(@Nullable BigDecimal value, @Nullable U unit) {
        return value != null && unit != null ? new EnteredValue<>(value.doubleValue(), -1, value.toString(), unit, DEFAULT, false) : null;
    }

    @Nullable
    public static <R extends MeasurementUnit> EnteredValue<R> add(@Nullable EnteredValue<R> left, @Nullable EnteredValue<R> right) {
        return addOrSubtract(left, right, 1.0);
    }

    @Nullable
    public static <R extends MeasurementUnit> EnteredValue<R> subtract(@Nullable EnteredValue<R> left, @Nullable EnteredValue<R> right) {
        return addOrSubtract(left, right, -1.0);
    }

    @Nullable
    private static <R extends MeasurementUnit> EnteredValue<R> addOrSubtract(@Nullable EnteredValue<R> left, @Nullable EnteredValue<R> right, double rightSign) {
        if (left == null || right == null) {
            return null;
        }
        checkArgument(left.unit.getClass().equals(right.unit.getClass()), "Inconvertible units: %s and %s", left.unit, right.unit);
        R unit;
        if (left.getValue() == 0.0 && right.getValue() != 0.0) {
            unit = right.unit;
        } else if (right.getValue() == 0.0 && left.getValue() != 0.0) {
            unit = left.unit;
        } else {
            unit = left.unit.getMultiplier() >= right.getUnit().getMultiplier() ? left.unit : right.unit;
        }
        return calculated(left.convert(unit) + rightSign * right.convert(unit), unit);
    }

    @Nullable
    public static <A extends MeasurementUnit, B extends MeasurementUnit, R extends MeasurementUnit> EnteredValue<R> multiply(@Nullable EnteredValue<A> left, @Nullable EnteredValue<B> right) {
        if (left == null || right == null) {
            return null;
        }
        MeasurementUtil.UnitAndMultiplier pair = MeasurementUtil.multiply(left.unit, right.unit);
        //noinspection unchecked
        return (EnteredValue<R>) calculated(left.value * right.value * pair.multiplier(), pair.unit());
    }

    public static <U extends MeasurementUnit> EnteredValue<U> multiply(EnteredValue<U> self, double by) {
        return calculated(self.value * by, self.unit);
    }

    @Nullable
    public static <R extends MeasurementUnit> EnteredValue<R> divide(@Nullable EnteredValue<?> left, @Nullable EnteredValue<?> right) {
        if (left == null || right == null) {
            return null;
        }
        MeasurementUtil.UnitAndMultiplier pair = MeasurementUtil.divide(left.unit, right.unit);
        //noinspection unchecked
        return (EnteredValue<R>) calculated(left.value / right.value * pair.multiplier(), pair.unit());
    }

    public static <U extends MeasurementUnit> EnteredValue<U> divide(EnteredValue<U> self, double by) {
        return calculated(self.value / by, self.unit);
    }

    @JsonProperty("value")
    public String getStringValue() {
        if (stringValue == null) {
            Preconditions.checkState(significantFigures != -1);
            stringValue = formatToSignificantFigures(value, significantFigures);
        }
        return stringValue;
    }

    public boolean valueEquals(EnteredValue<?> other) {
        checkArgument(unit.getClass().equals(other.unit.getClass()), "Non-comparable units: %s and %s", unit, other.unit);
        double thisValue = value * unit.getMultiplier();
        double otherValue = other.value * other.unit.getMultiplier();
        return Precision.equalsWithRelativeTolerance(thisValue, otherValue, 1e-6);
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(getStringValue());
    }

    public EnteredValue<U> withOverwritten(boolean overwritten) {
        return new EnteredValue<>(value, significantFigures, stringValue, unit, source, overwritten);
    }

    private double convert(U toUnit) {
        return value * unit.getMultiplier() / toUnit.getMultiplier();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(source).append(": ").append(getStringValue());
        if (unit != NoUnit.NO_UNIT) {
            str.append(' ').append(unit);
        }
        return str.toString();
    }
}
