package com.epam.indigoeln.eln.util;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public class Conditions {

    public static Conditions EMPTY = new Conditions();

    private static final Pattern QUESTION_MARK = Pattern.compile("\\?");

    private final List<String> fields = new ArrayList<>();
    private final List<@Nullable Object> values = new ArrayList<>();
    private int paramNo = 0;

    public Conditions add(String field) {
        fields.add(field);
        return this;
    }

    public Conditions add(String field, @Nullable Object... value) {
        int count = 0;
        Matcher matcher = QUESTION_MARK.matcher(field);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "?" + (++paramNo));
            values.add(value[count++]);
        }
        matcher.appendTail(sb);
        field = sb.toString();
        checkArgument(count == value.length, "condition must contain exactly %s ? character: %s", value.length, field);
        fields.add(field);
        return this;
    }

    public Conditions addIf(boolean condition, String field) {
        if (condition) {
            add(field);
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
        return fields.isEmpty() ? "true" : String.join(" and ", fields);
    }

    public Object[] getValues() {
        return values.toArray();
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
