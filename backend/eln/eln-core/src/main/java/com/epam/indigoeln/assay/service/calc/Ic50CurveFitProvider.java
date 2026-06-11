package com.epam.indigoeln.assay.service.calc;

import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.Nullable;

/**
 * Four-parameter logistic (4PL) dose-response fit producing an IC50/EC50. The non-linear
 * regression itself is not yet implemented; the provider is registered so the calculation id
 * is discoverable and wired through the engine, and fails loudly until the fit lands.
 */
public final class Ic50CurveFitProvider implements CalculationProvider {

    @Override
    public String id() {
        return LibraryCalculations.IC50_4PL;
    }

    @Override
    public CalculationResult evaluate(CalculationContext context, @Nullable JsonNode params) {
        throw new UnsupportedOperationException(
                "IC50_4PL curve fitting is not yet implemented (requires non-linear regression)");
    }
}
