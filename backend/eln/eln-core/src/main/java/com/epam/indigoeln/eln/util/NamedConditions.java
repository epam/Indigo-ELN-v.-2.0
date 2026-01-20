package com.epam.indigoeln.eln.util;

import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NamedConditions {

    public static NamedConditions EMPTY = new NamedConditions();

    private final List<String> expressions = new ArrayList<>();
    @Getter
    private final Map<String, @Nullable Object> values = new LinkedHashMap<>();

    public NamedConditions add(String expression) {
        expressions.add(expression);
        return this;
    }

    public NamedConditions add(String expression, String parameterName, @Nullable Object parameterValue) {
        expressions.add(expression);
        values.put(parameterName, parameterValue);
        return this;
    }

    public NamedConditions addIfNotNull(String expression, String parameterName, @Nullable Object parameterValue) {
        if (parameterValue != null) {
            add(expression, parameterName, parameterValue);
        }
        return this;
    }

    public String getQuery() {
        return expressions.isEmpty() ? "true" : String.join(" and ", expressions);
    }

    public boolean isEmpty() {
        return expressions.isEmpty();
    }
}
