package com.epam.indigoeln.reaction.model.units;

import com.epam.indigoeln.reaction.util.MeasurementUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.math3.util.Precision;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.math.BigDecimal;

import static com.epam.indigoeln.reaction.model.units.EnteredValueSource.DEFAULT;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.*;
import static com.google.common.base.Preconditions.*;

@EqualsAndHashCode(of = {"stringValue", "unit", "source", "present", "exact"})
@JsonSerialize(using = EnteredValue.Serializer.class)
public final class EnteredValue<U extends MeasurementUnit> {

    public static final EnteredValue<NoUnit> DEFAULT_ONE = defaultValue(1.0, 1, NoUnit.NO_UNIT);
    public static final EnteredValue<NoUnit> DEFAULT_ONE_HUNDRED = defaultValue(100.0, 1, NoUnit.NO_UNIT);

    private static final EnteredValue<?> EMPTY = new EnteredValue<>();
    private static final EnteredValue<?> EMPTY_OVERWRITTEN = new EnteredValue<>().withOverwritten(true);

    private final boolean present;

    private final boolean exact;

    @JsonIgnore
    private final double value;

    private final U unit;

    @JsonIgnore
    private final int significantFigures; // used only to lazily format stringValue

    @Getter
    private final EnteredValueSource source;

    @Nullable
    private String stringValue;

    @Getter
    private final boolean overwritten;

    @SuppressWarnings("unchecked")
    private EnteredValue() {
        this.present = false;
        this.exact = false;
        this.value = 0.0;
        this.significantFigures = 0;
        this.stringValue = "0";
        this.unit = (U) NoUnit.NO_UNIT;
        this.source = DEFAULT;
        this.overwritten = false;
    }

    @JsonCreator
    static <U extends MeasurementUnit> EnteredValue<U> fromJSON(@Nullable String stringValue, @Nullable Double exactValue, @Nullable U unit, @Nullable EnteredValueSource source, @Nullable Boolean overwritten) {
        if (stringValue == null && overwritten == null) {
            return empty();
        }
        boolean overwrittenTrue = Boolean.TRUE.equals(overwritten);
        if (exactValue != null) {
            return new EnteredValue<U>(true, exactValue, true, -1, stringValue, checkNotNull(unit), checkNotNull(source), overwrittenTrue);
        }
        if (stringValue != null) {
            return new EnteredValue<U>(true, Double.parseDouble(stringValue), false, -1, stringValue, checkNotNull(unit), checkNotNull(source), overwrittenTrue);
        }
        //noinspection unchecked
        return (EnteredValue<U>) EMPTY_OVERWRITTEN;
    }

    private EnteredValue(boolean present, double value, boolean exact, int significantFigures, @Nullable String stringValue, U unit, EnteredValueSource source, boolean overwritten) {
        this.present = present;
        this.value = value;
        this.exact = exact;
        this.significantFigures = significantFigures;
        this.stringValue = stringValue;
        this.unit = unit;
        this.source = source;
        this.overwritten = overwritten;
    }

    @SuppressWarnings("unchecked")
    public static <U extends MeasurementUnit> EnteredValue<U> empty() {
        return (EnteredValue<U>) EMPTY;
    }

    public static <U extends MeasurementUnit> EnteredValue<U> fixed(@Nullable Double value, int precision, U unit) {
        return value != null ? new EnteredValue<>(true, roundToSignificantFigures(value, precision), false, precision, null, unit, EnteredValueSource.FIXED, false) : empty();
    }

    public static <U extends MeasurementUnit> EnteredValue<U> fixedExact(@Nullable Double value, int decimalPlaces, U unit) {
        return value != null ? new EnteredValue<>(true, value, true, -1, roundToDecimalPlaces(value, decimalPlaces).toString(), unit, EnteredValueSource.FIXED, false) : empty();
    }

    public static <U extends MeasurementUnit> EnteredValue<U> userEntered(@Nullable String stringValue, @Nullable U unit, int revision) {
        return stringValue != null && unit != null ? new EnteredValue<U>(true, Double.parseDouble(stringValue), false, -1, stringValue, unit, EnteredValueSource.userEntered(revision), false) : empty();
    }

    public static <U extends MeasurementUnit> EnteredValue<U> calculated(@Nullable Double value, U unit) {
        return value != null ? new EnteredValue<>(true, value, false, getSignificantFigures(), null, unit, EnteredValueSource.CALCULATED, false) : empty();
    }

    public static <U extends MeasurementUnit> EnteredValue<U> defaultValue(@Nullable Double value, int precision, @Nullable U unit) {
        return value != null && unit != null ? new EnteredValue<U>(true, roundToSignificantFigures(value, precision), false, precision, null, unit, DEFAULT, false) : empty();
    }

    public static <U extends MeasurementUnit> EnteredValue<U> defaultValue(@Nullable BigDecimal value, @Nullable U unit) {
        return value != null && unit != null ? new EnteredValue<U>(true, value.doubleValue(), false, -1, value.toString(), unit, DEFAULT, false) : empty();
    }

    public static <R extends MeasurementUnit> EnteredValue<R> add(EnteredValue<R> left, EnteredValue<R> right) {
        return addOrSubtract(left, right, 1.0);
    }

    public static <R extends MeasurementUnit> EnteredValue<R> subtract(EnteredValue<R> left, EnteredValue<R> right) {
        return addOrSubtract(left, right, -1.0);
    }

    private static <R extends MeasurementUnit> EnteredValue<R> addOrSubtract(EnteredValue<R> left, EnteredValue<R> right, double rightSign) {
        if (left.isEmpty() || right.isEmpty()) {
            return empty();
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

    public static <A extends MeasurementUnit, B extends MeasurementUnit, R extends MeasurementUnit> EnteredValue<R> multiply(EnteredValue<A> left, EnteredValue<B> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return empty();
        }
        MeasurementUtil.UnitAndMultiplier pair = MeasurementUtil.multiply(left.unit, right.unit);
        //noinspection unchecked
        return (EnteredValue<R>) calculated(left.value * right.value * pair.multiplier(), pair.unit());
    }

    public static <U extends MeasurementUnit> EnteredValue<U> multiply(EnteredValue<U> self, double by) {
        return calculated(self.value * by, self.unit);
    }

    public static <R extends MeasurementUnit> EnteredValue<R> divide(EnteredValue<?> left, EnteredValue<?> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return empty();
        }
        MeasurementUtil.UnitAndMultiplier pair = MeasurementUtil.divide(left.unit, right.unit);
        //noinspection unchecked
        return (EnteredValue<R>) calculated(left.value / right.value * pair.multiplier(), pair.unit());
    }

    public static <U extends MeasurementUnit> EnteredValue<U> divide(EnteredValue<U> self, double by) {
        return calculated(self.value / by, self.unit);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return !present;
    }

    public double getValue() {
        checkState(present);
        return value;
    }

    public U getUnit() {
        checkState(present);
        return unit;
    }

    @JsonProperty("value")
    public String getStringValue() {
        if (stringValue == null) {
            checkState(significantFigures != -1);
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
        return new EnteredValue<>(present, value, exact, significantFigures, stringValue, unit, source, overwritten);
    }

    private double convert(U toUnit) {
        return value * unit.getMultiplier() / toUnit.getMultiplier();
    }

    @Override
    public String toString() {
        if (!present && !overwritten) {
            return "EMPTY";
        }
        StringBuilder str = new StringBuilder();
        if (present) {
            str.append(source).append(": ").append(getStringValue());
            if (unit != NoUnit.NO_UNIT) {
                str.append(' ').append(unit);
            }
        }
        if (overwritten) {
            if (!str.isEmpty()) {
                str.append(' ');
            }
            str.append("(overwritten)");
        }
        return str.toString();
    }

    public static class Serializer extends JsonSerializer<EnteredValue<?>> {

        @Override
        public boolean isEmpty(SerializerProvider provider, @Nullable EnteredValue<?> value) {
            return value == null || (!value.present && !value.overwritten);
        }

        @Override
        public void serialize(EnteredValue<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            if (value.present) {
                gen.writeStringField("value", value.getStringValue());
                if (value.exact) {
                    gen.writeNumberField("exactValue", value.value);
                }
                gen.writeObjectField("unit", value.unit);
                gen.writeObjectField("source", value.source);
            }
            if (value.overwritten) {
                gen.writeBooleanField("overwritten", true);
            }
            gen.writeEndObject();
        }
    }
}
