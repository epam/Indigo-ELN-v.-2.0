package com.epam.indigoeln.assay.service.calc;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Outcome of a calculation: a primary numeric value plus an optional structured payload
 * (e.g. curve-fit parameters and fit statistics).
 */
public record CalculationResult(double value, @Nullable Map<String, Object> payload) {

    public static CalculationResult of(double value) {
        return new CalculationResult(value, null);
    }

    public static CalculationResult of(double value, Map<String, Object> payload) {
        return new CalculationResult(value, payload);
    }
}
