package com.epam.indigoeln.assay.service.calc;

import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.Nullable;

/**
 * Aggregation providers over a single named vector input {@code "values"} (e.g. replicate wells).
 */
public final class BasicStatProviders {

    private BasicStatProviders() {
    }

    public static final class Mean implements CalculationProvider {
        @Override
        public String id() {
            return LibraryCalculations.MEAN;
        }

        @Override
        public CalculationResult evaluate(CalculationContext context, @Nullable JsonNode params) {
            return CalculationResult.of(Statistics.mean(context.vector("values")));
        }
    }

    public static final class StandardDeviation implements CalculationProvider {
        @Override
        public String id() {
            return LibraryCalculations.SD;
        }

        @Override
        public CalculationResult evaluate(CalculationContext context, @Nullable JsonNode params) {
            return CalculationResult.of(Statistics.stdDev(context.vector("values")));
        }
    }

    public static final class CoefficientOfVariation implements CalculationProvider {
        @Override
        public String id() {
            return LibraryCalculations.CV;
        }

        @Override
        public CalculationResult evaluate(CalculationContext context, @Nullable JsonNode params) {
            return CalculationResult.of(Statistics.cv(context.vector("values")));
        }
    }
}
