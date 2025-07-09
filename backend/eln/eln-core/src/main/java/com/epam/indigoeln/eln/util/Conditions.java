package com.epam.indigoeln.eln.util;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Conditions {

    public static Conditions EMPTY = new Conditions();

    private final List<String> fields = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();
    private int paramNo = 0;

    public Conditions add(String field) {
        int p = field.indexOf('?');
        Preconditions.checkArgument(p == -1, "no-parameter condition must not contain ? character: %s", field);
        fields.add(field);
        return this;
    }

    public Conditions add(String field, Object value) {
        int p = field.indexOf('?');
        Preconditions.checkArgument(p != -1, "condition must contain ? character: %s", field);
        field = field.substring(0, p + 1) + (++paramNo) + field.substring(p + 1);
        fields.add(field);
        values.add(value);
        return this;
    }

    public Conditions addNullable(String fieldIfNotNull, String fieldIfNull, @Nullable Object value) {
        if (value != null) {
            add(fieldIfNotNull, value);
        } else {
            add(fieldIfNull);
        }
        return this;
    }

    public Conditions addIf(boolean condition, String field) {
        if (condition) {
            add(field);
        }
        return this;
    }

    public Conditions addIf(boolean condition, String field, Object value) {
        if (condition) {
            add(field, value);
        }
        return this;
    }

    public Conditions addIfNotNull(String field, @Nullable Object value) {
        if (value != null) {
            add(field, value);
        }
        return this;
    }

    public String getQuery() {
        return String.join(" and ", fields);
    }

    public Object[] getValues() {
        return values.toArray();
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
