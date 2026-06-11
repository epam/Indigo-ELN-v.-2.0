package com.epam.indigoeln.assay.service.calc;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Named numeric inputs supplied to a {@link CalculationProvider}. Each named role maps to a
 * vector of values (a single well contributes a length-1 vector; a control group contributes
 * one value per replicate well).
 */
public final class CalculationContext {

    private final Map<String, double[]> inputs;

    public CalculationContext(Map<String, double[]> inputs) {
        this.inputs = Map.copyOf(inputs);
    }

    public boolean has(String role) {
        return inputs.containsKey(role);
    }

    public double[] vector(String role) {
        double[] values = inputs.get(role);
        if (values == null) {
            throw new NoSuchElementException("Missing calculation input: " + role);
        }
        return values;
    }

    public double scalar(String role) {
        double[] values = vector(role);
        if (values.length != 1) {
            throw new IllegalArgumentException("Input '" + role + "' must be a single value but had " + values.length);
        }
        return values[0];
    }

    @Nullable
    public double[] optionalVector(String role) {
        return inputs.get(role);
    }

    public Map<String, double[]> inputs() {
        return inputs;
    }
}
