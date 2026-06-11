package com.epam.indigoeln.assay.service.calc;

import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.Nullable;

/**
 * A built-in calculation. Implementations are pure functions of their {@link CalculationContext}
 * inputs and optional JSON parameters, so they are deterministic and unit-testable.
 */
public interface CalculationProvider {

    /** Stable identifier stored in {@code Calculation.library_id}. */
    String id();

    CalculationResult evaluate(CalculationContext context, @Nullable JsonNode params);
}
