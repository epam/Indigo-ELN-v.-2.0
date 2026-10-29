package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.reaction.util.UnitUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Precision;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

import static com.epam.indigoeln.reaction.util.CalculationContext.getSignificantFigures;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.formatToSignificantFigures;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.roundToDecimalPlaces;
import static com.epam.indigoeln.reaction.util.UnitUtils.convertFromBase;
import static com.epam.indigoeln.reaction.util.UnitUtils.convertToBase;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonSerialize(using = EnteredValue.Serializer.class)
@JsonDeserialize(using = EnteredValue.Deserializer.class)
public sealed abstract class EnteredValue<U extends MeasurementUnit> permits EnteredValue.Absent, EnteredValue.Fixed, EnteredValue.User, EnteredValue.Calculated, EnteredValue.Default {

    public static final EnteredValue<NoUnit> DEFAULT_ONE = defaultValue(1.0, NoUnit.NO_UNIT, 1);
    public static final EnteredValue<NoUnit> DEFAULT_ONE_HUNDRED = defaultValue(100.0, NoUnit.NO_UNIT, 1);

    private static final int FIXED_VALUE = Integer.MAX_VALUE;
    private static final String FIXED_STR = "F";
    private static final int DEFAULT_VALUE = Integer.MIN_VALUE;
    private static final String DEFAULT_STR = "D";
    private static final int CALCULATED_VALUE = -1;
    private static final String CALCULATED_STR = "C";
    private static final String USER_STR = "U";
    private static final String CLEARED_STR = "L";

    static <U extends MeasurementUnit> EnteredValue<U> fromJSON(@Nullable String value, @Nullable Double exactValue, @Nullable String source, @Nullable Integer revision, U unit) {
        if (source == null) {
            checkArgument(value == null && exactValue == null && revision == null);
            return empty();
        }
        return switch (source) {
            case CALCULATED_STR -> {
                checkArgument(exactValue == null && revision == null);
                yield new Calculated<>(checkNotNull(value), unit);
            }
            case DEFAULT_STR -> {
                checkArgument(exactValue == null && revision == null);
                yield new Default<>(checkNotNull(value), unit);
            }
            case FIXED_STR -> {
                checkArgument(revision == null);
                yield new Fixed<>(checkNotNull(value), unit, checkNotNull(exactValue));
            }
            case USER_STR -> {
                checkArgument(exactValue == null);
                yield new UserEntered<>(checkNotNull(value), unit, checkNotNull(revision));
            }
            case CLEARED_STR -> {
                checkArgument(value == null && exactValue == null);
                yield new UserCleared<>(checkNotNull(revision));
            }
            default -> throw new IllegalArgumentException("Unknown source: " + source);
        };
    }

    public static <U extends MeasurementUnit> EnteredValue.Absent<U> empty() {
        //noinspection unchecked
        return (Absent<U>) Absent.INSTANCE;
    }

    public static <U extends MeasurementUnit> EnteredValue<U> fixed(@Nullable Double value, U unit, int decimalPlaces) {
        return value != null ? new Fixed<>(value, unit, decimalPlaces) : empty();
    }

    public static <U extends MeasurementUnit> EnteredValue<U> userEntered(@Nullable String stringValue, @Nullable U unit, int revision) {
        return stringValue != null && unit != null ? new UserEntered<>(stringValue, unit, revision) : new UserCleared<>(revision);
    }

    public static <U extends MeasurementUnit> EnteredValue<U> calculated(@Nullable Double value, @Nullable U unit) {
        return value != null ? new EnteredValue.Calculated<>(value, checkNotNull(unit)) : empty();
    }

    public static <U extends MeasurementUnit> EnteredValue<U> defaultValue(@Nullable Double value, U unit, int precision) {
        return value != null ? new EnteredValue.Default<>(formatToSignificantFigures(value, precision), unit) : empty();
    }

    public static <U extends MeasurementUnit> EnteredValue<U> defaultValue(@Nullable BigDecimal value, U unit) {
        return value != null ? new EnteredValue.Default<>(value.toPlainString(), unit) : empty();
    }

    @JsonIgnore
    public boolean hasValue() {
        return switch (this) {
            case Absent<?> _, UserCleared<?> _ -> false;
            default -> true;
        };
    }

    public boolean isDefault() {
        return this instanceof EnteredValue.Default<U>;
    }

    public boolean isCalculated() {
        return this instanceof EnteredValue.Calculated<U>;
    }

    public boolean isUser() {
        return this instanceof EnteredValue.User<U>;
    }

    public boolean isUserEntered() {
        return this instanceof EnteredValue.UserEntered<U>;
    }

    public boolean isUserCleared() {
        return this instanceof EnteredValue.UserCleared<U>;
    }

    public boolean isFixed() {
        return this instanceof EnteredValue.Fixed<U>;
    }

    public double getBaseValue() {
        return switch (this) {
            case EnteredValue.Absent<U> _ -> throw new IllegalStateException("getBaseValue on Absent");
            case EnteredValue.Calculated<U> v -> v.baseValue;
            case EnteredValue.Default<U> v -> v.baseValue;
            case EnteredValue.Fixed<U> v -> v.baseValue;
            case EnteredValue.UserEntered<U> v -> v.baseValue;
            case EnteredValue.UserCleared<U> _ -> throw new IllegalStateException("getBaseValue on UserCleared");
        };
    }

    @Nullable
    public Double getValueOrNull() {
        return switch (this) {
            case EnteredValue.Absent<U> _ -> null;
            case EnteredValue.Calculated<U> v -> v.value;
            case EnteredValue.Default<U> v -> v.value;
            case EnteredValue.Fixed<U> v -> v.value;
            case EnteredValue.UserEntered<U> v -> v.value;
            case EnteredValue.UserCleared<U> _ -> null;
        };
    }

    public U getUnit() {
        return switch (this) {
            case EnteredValue.Absent<U> v -> throw new IllegalStateException("getUnit on Absent");
            case EnteredValue.Calculated<U> v -> v.unit;
            case EnteredValue.Default<U> v -> v.unit;
            case EnteredValue.Fixed<U> v -> v.unit;
            case EnteredValue.UserEntered<U> v -> v.unit;
            case EnteredValue.UserCleared<U> _ -> throw new IllegalStateException("getUnit on UserCleared");
        };
    }

    @Nullable
    public String getStringValue() {
        return switch (this) {
            case EnteredValue.Absent<U> v -> null;
            case EnteredValue.Calculated<U> v -> {
                if (v.stringValue == null) {
                    v.stringValue = formatToSignificantFigures(v.value, getSignificantFigures());
                }
                yield v.stringValue;
            }
            case EnteredValue.Default<U> v -> v.stringValue;
            case EnteredValue.Fixed<U> v -> v.stringValue;
            case EnteredValue.UserEntered<U> v -> v.stringValue;
            case EnteredValue.UserCleared<U> _ -> null;
        };
    }

    public int getPriority() {
        return switch (this) {
            case EnteredValue.Absent<U> _ -> throw new IllegalStateException();
            case EnteredValue.Calculated<U> _ -> CALCULATED_VALUE;
            case EnteredValue.Default<U> _ -> DEFAULT_VALUE;
            case EnteredValue.Fixed<U> _ -> FIXED_VALUE;
            case EnteredValue.User<U> v -> v.revision;
        };
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(checkNotNull(getStringValue()));
    }

    @Nullable
    public BigDecimal toBigDecimalOrNull() {
        return hasValue() ? toBigDecimal() : null;
    }

    public boolean valueEquals(EnteredValue<?> other) {
        return Precision.equalsWithRelativeTolerance(getBaseValue(), other.getBaseValue(), 1e-6);
    }

    @Override
    public String toString() {
        return switch (this) {
            case EnteredValue.Absent<U> _ -> "empty";
            case EnteredValue.Calculated<U> v -> "calculated: %s %s (base %s)".formatted(v.getStringValue(), v.getUnit(), v.getBaseValue());
            case EnteredValue.Default<U> v -> "default: %s %s".formatted(v.getStringValue(), v.getUnit());
            case EnteredValue.Fixed<U> v -> "fixed: %s %s".formatted(v.getStringValue(), v.getUnit());
            case EnteredValue.UserEntered<U> v -> "user-entered: %s %s".formatted(v.getStringValue(), v.getUnit());
            case EnteredValue.UserCleared<U> _ -> "user-cleared";
        };
    }

    public String toUserFriendlyString(boolean useFriendlyUnitNames, String emptyString) {
        if (!hasValue()) {
            return emptyString;
        }
        StringBuilder str = new StringBuilder(checkNotNull(getStringValue()));
        if (getUnit() != NoUnit.NO_UNIT) {
            str.append(' ').append(useFriendlyUnitNames ? getUnit().getDisplayName() : getUnit().name());
        }
        return str.toString();
    }

    @EqualsAndHashCode(callSuper = false)
    public static final class Absent<U extends MeasurementUnit> extends EnteredValue<U> {

        private static final EnteredValue<?> INSTANCE = new EnteredValue.Absent<>();
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static final class Fixed<U extends MeasurementUnit> extends EnteredValue<U> {

        private final double value; // exact value, also serialized
        private final U unit;
        private final String stringValue;
        private final double baseValue;

        public Fixed(double value, U unit, int decimalPlaces ) {
            this.value = value;
            this.unit = unit;
            this.stringValue = roundToDecimalPlaces(value, decimalPlaces).toPlainString();
            baseValue = convertToBase(value, unit);
        }

        public Fixed(String stringValue, U unit, double value) {
            this.value = value;
            this.unit = unit;
            this.stringValue = stringValue;
            baseValue = convertToBase(value, unit);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    public static sealed abstract class User<U extends MeasurementUnit> extends EnteredValue<U> permits UserEntered, UserCleared {

        @Getter
        private final int revision;

        protected User(int revision) {
            this.revision = revision;
        }
    }

    @EqualsAndHashCode(of = "stringValue", callSuper = true)
    public static final class UserEntered<U extends MeasurementUnit> extends User<U> {

        @Getter(AccessLevel.NONE)
        private final double value;
        private final U unit;
        private final double baseValue;
        private final String stringValue;

        public UserEntered(String stringValue, U unit, int revision) {
            super(revision);
            this.stringValue = stringValue;
            this.unit = unit;
            value = Double.parseDouble(stringValue);
            baseValue = convertToBase(value, unit);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    public static final class UserCleared<U extends MeasurementUnit> extends User<U> {

        public UserCleared(int revision) {
            super(revision);
        }
    }

    public static final class Calculated<U extends MeasurementUnit> extends EnteredValue<U> {

        private final double baseValue;
        private final U unit;
        private final double value;
        @Nullable
        private String stringValue;

        public Calculated(double baseValue, U unit) {
            this.baseValue = baseValue;
            this.unit = unit;
            value = convertFromBase(baseValue, unit);
        }

        public Calculated(String stringValue, U unit) {
            this.stringValue = stringValue;
            this.unit = unit;
            value = Double.parseDouble(stringValue);
            baseValue = convertToBase(value, unit);
        }

        public Calculated<U> withUnit(U newUnit) {
            return new Calculated<>(baseValue, newUnit);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Calculated<?> that && unit == that.unit && Objects.equals(getStringValue(), that.getStringValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(unit, getStringValue());
        }
    }

    @EqualsAndHashCode(of = "stringValue", callSuper = false)
    public static final class Default<U extends MeasurementUnit> extends EnteredValue<U> {

        private final double value;
        private final U unit;
        private final double baseValue;
        private final String stringValue;

        public Default(String stringValue, U unit) {
            this.stringValue = stringValue;
            this.unit = unit;
            value = Double.parseDouble(stringValue);
            baseValue = convertToBase(value, unit);
        }
    }

    public static class Serializer extends JsonSerializer<EnteredValue<?>> {

        @Override
        public boolean isEmpty(SerializerProvider provider, @Nullable EnteredValue<?> value) {
            return switch (value) {
                case null -> true;
                case Absent<?> _ -> true;
                default -> false;
            };
        }

        @Override
        public void serialize(EnteredValue<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            switch (value) {
                case Absent<?> _ -> {}
                case Calculated<?> _ -> gen.writeStringField("source", CALCULATED_STR);
                case Default<?> _ -> gen.writeStringField("source", DEFAULT_STR);
                case Fixed<?> v -> {
                    gen.writeStringField("source", FIXED_STR);
                    gen.writeNumberField("exact", v.value);
                }
                case UserEntered<?> v -> {
                    gen.writeStringField("source", USER_STR);
                    gen.writeNumberField("revision", v.getRevision());
                }
                case UserCleared<?> v -> {
                    gen.writeStringField("source", CLEARED_STR);
                    gen.writeNumberField("revision", v.getRevision());
                }
            }
            if (value.hasValue()) {
                gen.writeStringField("value", value.getStringValue());
            }
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends StdDeserializer<EnteredValue<?>> implements ContextualDeserializer {

        @Nullable
        private final Class<?> unitType;

        public Deserializer() {
            this(null);
        }

        private Deserializer(@Nullable Class<?> unitType) {
            super(EnteredValue.class);
            this.unitType = unitType;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
            JavaType type = property != null ? property.getType() : ctxt.getContextualType();
            return new Deserializer(type.containedType(0).getRawClass());
        }

        @Override
        public EnteredValue<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            UnitSource units = (UnitSource) ctxt.getAttribute(UnitSource.CONTEXT_KEY);
            MeasurementUnit unit = UnitUtils.getContextUnitByType(checkNotNull(unitType), units);
            return fromJSON(
                    node.hasNonNull("value") ? node.get("value").asText() : null,
                    node.hasNonNull("exact") ? node.get("exact").asDouble() : null,
                    node.hasNonNull("source") ? node.get("source").asText() : null,
                    node.hasNonNull("revision") ? node.get("revision").asInt() : null,
                    unit);
        }
    }
}
