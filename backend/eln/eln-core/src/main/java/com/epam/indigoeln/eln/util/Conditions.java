package com.epam.indigoeln.eln.util;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Conditions {

    public static Conditions EMPTY = new Conditions();

    private final List<List<String>> fields;
    private final List<Object> values = new ArrayList<>();
    private int paramNo = 0;
    @Setter
    private int slotNo = 0;

    public Conditions() {
        this(1);
    }

    public Conditions(int slotCount) {
        fields = new ArrayList<>();
        for (int i = 0; i < slotCount; i++) {
            fields.add(new ArrayList<>());
        }
    }

    public Conditions add(String field) {
        fields.get(slotNo).add(field);
        return this;
    }

    public Conditions add(int slotNo, String field) {
        fields.get(slotNo).add(field);
        return this;
    }

    public Conditions add(String field, @Nullable Object value) {
        return add(slotNo, field, value);
    }

    public Conditions add(int slotNo, String field, @Nullable Object value) {
        int p = field.indexOf('?');
        Preconditions.checkArgument(p != -1, "condition must contain ? character: %s", field);
        field = field.substring(0, p + 1) + (++paramNo) + field.substring(p + 1);
        fields.get(slotNo).add(field);
        values.add(value);
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
        return getQuery(0);
    }

    public String getQuery(int slotNo) {
        return fields.get(slotNo).isEmpty() ? "true" : String.join(" and ", fields.get(slotNo));
    }

    public Object[] getValues() {
        return values.toArray();
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
