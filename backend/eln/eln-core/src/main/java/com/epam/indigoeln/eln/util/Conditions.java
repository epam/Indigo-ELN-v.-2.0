package com.epam.indigoeln.eln.util;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Conditions {

    public static Conditions EMPTY = new Conditions();

    private final List<String> fields = new ArrayList<>();
    private final List<@Nullable Object> values = new ArrayList<>();
    private int paramNo = 0;

    public Conditions add(String field) {
        fields.add(field);
        return this;
    }

    public Conditions add(String field, @Nullable Object value) {
        int p = field.indexOf('?');
        Preconditions.checkArgument(p != -1, "condition must contain ? character: %s", field);
        field = field.substring(0, p + 1) + (++paramNo) + field.substring(p + 1);
        fields.add(field);
        values.add(value);
        return this;
    }

    public Conditions addIfNotNull(String field, @Nullable Object value) {
        if (value != null) {
            add(field, value);
        }
        return this;
    }

    public String getQuery() {
        return fields.isEmpty() ? "true" : String.join(" and ", fields);
    }

    public Object[] getValues() {
        return values.toArray();
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
