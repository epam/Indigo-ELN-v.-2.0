package com.epam.indigoeln.reaction.model.units;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EnteredValueOpt<U extends MeasurementUnit> {

    private static final EnteredValueOpt<MeasurementUnit> EMPTY = new EnteredValueOpt<>(null);

    @Nullable
    private final EnteredValue<U> value;

    public static <U extends MeasurementUnit> EnteredValueOpt<U> opt(@Nullable EnteredValue<U> value) {
        return value != null ? new EnteredValueOpt<>(value) : empty();
    }

    public boolean isPresent() {
        return value != null;
    }

    public EnteredValueOpt<U> add(@Nullable EnteredValue<U> other) {
        if (value == null || other == null) {
            return empty();
        }
        return EnteredValueOpt.opt(EnteredValue.add(value, other));
    }

    public EnteredValueOpt<U> add(EnteredValueOpt<U> other) {
        return add(other.value);
    }

    public EnteredValueOpt<U> subtract(@Nullable EnteredValue<U> other) {
        if (value == null || other == null) {
            return empty();
        }
        return EnteredValueOpt.opt(EnteredValue.subtract(value.cast(), other));
    }

    public EnteredValueOpt<U> subtract(EnteredValueOpt<U> other) {
        return subtract(other.value);
    }

    public <R extends MeasurementUnit> EnteredValueOpt<R> multiply(@Nullable EnteredValue<?> by) {
        if (value == null || by == null) {
            return empty();
        }
        return EnteredValueOpt.opt(EnteredValue.multiply(value, by));
    }

    public EnteredValueOpt<U> multiply(@Nullable Double by) {
        if (value == null || by == null) {
            return empty();
        }
        return EnteredValueOpt.opt(EnteredValue.multiply(value, by));
    }

    public <R extends MeasurementUnit> EnteredValueOpt<R> divide(@Nullable EnteredValue<?> by) {
        if (value == null || by == null) {
            return empty();
        }
        return EnteredValueOpt.opt(EnteredValue.divide(value, by));
    }

    public EnteredValueOpt<U> divide(@Nullable Double by) {
        if (value == null || by == null) {
            return empty();
        }
        return EnteredValueOpt.opt(EnteredValue.divide(value, by));
    }

    private static <U extends MeasurementUnit> EnteredValueOpt<U> empty() {
        //noinspection unchecked
        return (EnteredValueOpt<U>) EMPTY;
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "EMPTY";
    }
}
