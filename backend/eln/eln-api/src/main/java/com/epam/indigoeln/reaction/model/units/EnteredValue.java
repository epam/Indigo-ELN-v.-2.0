package com.epam.indigoeln.reaction.model.units;

import com.epam.indigoeln.reaction.util.MeasurementUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.Precision;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

import static com.epam.indigoeln.reaction.model.units.EnteredValueSource.DEFAULT;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.formatToSignificantFigures;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.roundToSignificantFigures;

@Getter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class EnteredValue<U extends MeasurementUnit> {

    public static final EnteredValue<NoUnit> DEFAULT_ONE = defaultValue(1.0, 1, NoUnit.NO_UNIT);
    public static final EnteredValue<NoUnit> DEFAULT_ONE_HUNDRED = defaultValue(100.0, 1, NoUnit.NO_UNIT);
    public static final EnteredValue<MolUnit> ZERO_MOL = defaultValue(0.0, 0, MolUnit.MOL);

    @JsonIgnore
    private final double value;
    private final U unit;
    @JsonProperty("value")
    private final String stringValue; // for now, always set; maybe postpone initialization for calculated values if gets recalculated too often
    @Setter
    private EnteredValueSource source;
    @Deprecated // !!! only to deserialize existing models; remove
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean conflict = false;
    @Setter
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean overwritten = false;

    @JsonCreator
    EnteredValue(String stringValue, U unit, EnteredValueSource source) {
        this(Double.parseDouble(stringValue), stringValue, unit, source);
    }

    public EnteredValue(double value, String stringValue, U unit, EnteredValueSource source) {
        this.value = value;
        this.stringValue = stringValue;
        this.unit = unit;
        this.source = source;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> fixed(@Nullable Double value, int precision, U unit) {
        return value != null ? new EnteredValue<>(roundToSignificantFigures(value, precision), formatToSignificantFigures(value, precision), unit, EnteredValueSource.FIXED) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> fixed(@Nullable BigDecimal value, U unit) {
        return value != null ? new EnteredValue<>(value.doubleValue(), value.toString(), unit, EnteredValueSource.FIXED) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> userEntered(@Nullable String stringValue, @Nullable U unit, int revision) {
        return stringValue != null && unit != null ? new EnteredValue<>(Double.parseDouble(stringValue), stringValue, unit, EnteredValueSource.userEntered(revision)) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> calculated(@Nullable Double value, U unit, EnteredValue<?> from1, EnteredValue<?> from2) {
        return value != null ? new EnteredValue<>(value, formatToSignificantFigures(value), unit, EnteredValueSource.calculated(from1.source, from2.source)) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> defaultValue(@Nullable Double value, int precision, @Nullable U unit) {
        return value != null && unit != null ? new EnteredValue<>(roundToSignificantFigures(value, precision), formatToSignificantFigures(value, precision), unit, DEFAULT) : null;
    }

    @Nullable
    public static <U extends MeasurementUnit> EnteredValue<U> defaultValue(@Nullable BigDecimal value, @Nullable U unit) {
        return value != null && unit != null ? new EnteredValue<>(value.doubleValue(), value.toString(), unit, DEFAULT) : null;
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

    public BigDecimal toBigDecimal() {
        return new BigDecimal(stringValue);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(source).append(": ").append(stringValue);
        if (unit != NoUnit.NO_UNIT) {
            str.append(' ').append(unit);
        }
        if (overwritten) {
            str.append(" [OVERWRITTEN]");
        }
        return str.toString();
    }
}
