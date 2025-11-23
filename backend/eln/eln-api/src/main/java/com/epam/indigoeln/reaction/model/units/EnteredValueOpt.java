package com.epam.indigoeln.reaction.model.units;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EnteredValueOpt {

    private static final EnteredValueOpt EMPTY = new EnteredValueOpt(null);

    @Nullable
    private final EnteredValue<?> value;

    public static EnteredValueOpt opt(@Nullable EnteredValue<?> value) {
        return value != null ? new EnteredValueOpt(value) : EMPTY;
    }

    public boolean isPresent() {
        return value != null;
    }

    public EnteredValueOpt add(@Nullable EnteredValue<?> other) {
        if (value == null || other == null) {
            return EMPTY;
        }
        return EnteredValueOpt.opt(EnteredValue.add(value.cast(), other));
    }

    public EnteredValueOpt subtract(@Nullable EnteredValue<?> other) {
        if (value == null || other == null) {
            return EMPTY;
        }
        return EnteredValueOpt.opt(EnteredValue.subtract(value.cast(), other));
    }

    public EnteredValueOpt multiply(@Nullable EnteredValue<?> by) {
        if (value == null || by == null) {
            return EMPTY;
        }
        return EnteredValueOpt.opt(EnteredValue.multiply(value, by));
    }

    public EnteredValueOpt multiply(@Nullable Double by) {
        if (value == null || by == null) {
            return EMPTY;
        }
        return EnteredValueOpt.opt(EnteredValue.multiply(value, by));
    }

    public EnteredValueOpt divide(@Nullable EnteredValue<?> by) {
        if (value == null || by == null) {
            return EMPTY;
        }
        return EnteredValueOpt.opt(EnteredValue.divide(value, by));
    }

    public EnteredValueOpt divide(@Nullable Double by) {
        if (value == null || by == null) {
            return EMPTY;
        }
        return EnteredValueOpt.opt(EnteredValue.divide(value, by));
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "EMPTY";
    }
}
