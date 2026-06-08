package com.epam.indigoeln.reaction.model.units;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "priority")
public class EnteredValueSource {

    private static final int FIXED_VALUE = Integer.MAX_VALUE;
    private static final String FIXED_STR = "fixed";
    private static final int DEFAULT_VALUE = Integer.MIN_VALUE;
    private static final String DEFAULT_STR = "default";
    private static final int CALCULATED_VALUE = -1;
    private static final String CALCULATED_STR = "calculated";

    public static final EnteredValueSource FIXED = new EnteredValueSource(FIXED_VALUE);
    public static final EnteredValueSource DEFAULT = new EnteredValueSource(DEFAULT_VALUE);
    public static final EnteredValueSource CALCULATED = new EnteredValueSource(CALCULATED_VALUE);

    @Getter
    private final int priority;

    public static EnteredValueSource userEntered(int revision) {
        Preconditions.checkArgument(revision > 0);
        return new EnteredValueSource(revision);
    }

    private EnteredValueSource(int priority) {
        Preconditions.checkArgument(priority != 0);
        this.priority = priority;
    }

    @JsonCreator
    EnteredValueSource(Object from) {
        if (from.equals(FIXED_STR)) {
            this.priority = FIXED_VALUE;
        } else if (from.equals(DEFAULT_STR)) {
            this.priority = DEFAULT_VALUE;
        } else if (from.equals(CALCULATED_STR)) {
            this.priority = CALCULATED_VALUE;
        } else if (from instanceof Number number) {
            int intValue = number.intValue();
            Preconditions.checkArgument(intValue > 0);
            this.priority = intValue;
        } else {
            throw new IllegalArgumentException("Invalid EnteredValueSource: " + from);
        }
    }

    @JsonValue
    public Object getJSONValue() {
        return switch (priority) {
            case FIXED_VALUE -> FIXED_STR;
            case DEFAULT_VALUE -> DEFAULT_STR;
            case CALCULATED_VALUE -> CALCULATED_STR;
            default -> priority;
        };
    }

    @JsonIgnore
    public boolean isFixed() {
        return priority == FIXED_VALUE;
    }

    @JsonIgnore
    public boolean isDefault() {
        return priority == DEFAULT_VALUE;
    }

    @JsonIgnore
    public boolean isUserEntered() {
        return priority > 0 && priority != FIXED_VALUE;
    }

    @JsonIgnore
    public boolean isCalculated() {
        return priority == CALCULATED_VALUE;
    }

    @Override
    public String toString() {
        return switch (priority) {
            case FIXED_VALUE -> FIXED_STR;
            case DEFAULT_VALUE -> DEFAULT_STR;
            case CALCULATED_VALUE -> CALCULATED_STR;
            default -> "userEntered(" + priority + ")";
        };
    }
}
