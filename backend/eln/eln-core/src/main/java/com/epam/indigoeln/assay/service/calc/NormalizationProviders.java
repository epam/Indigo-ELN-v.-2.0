package com.epam.indigoeln.assay.service.calc;

import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.Nullable;

/**
 * Control-based normalisation providers.
 */
public final class NormalizationProviders {

    private NormalizationProviders() {
    }

    /**
     * Percent inhibition relative to the neutral (negative) control:
     * {@code 100 * (negMean - signal) / (negMean - posMean)} where the negative control shows no
     * inhibition and the positive control shows full inhibition.
     */
    public static final class PercentInhibition implements CalculationProvider {
        @Override
        public String id() {
            return LibraryCalculations.PERCENT_INHIBITION;
        }

        @Override
        public CalculationResult evaluate(CalculationContext context, @Nullable JsonNode params) {
            double signal = Statistics.mean(context.vector("signal"));
            double negMean = Statistics.mean(context.vector("negativeControl"));
            double posMean = Statistics.mean(context.vector("positiveControl"));
            double range = negMean - posMean;
            if (range == 0) {
                throw new ArithmeticException("Positive and negative controls have the same mean; cannot normalise");
            }
            return CalculationResult.of(100.0 * (negMean - signal) / range);
        }
    }

    /**
     * Linear normalisation of a signal onto a 0..100 scale defined by a low and high control:
     * {@code 100 * (signal - lowMean) / (highMean - lowMean)}.
     */
    public static final class NormalizeToControls implements CalculationProvider {
        @Override
        public String id() {
            return LibraryCalculations.NORMALIZE_TO_CONTROLS;
        }

        @Override
        public CalculationResult evaluate(CalculationContext context, @Nullable JsonNode params) {
            double signal = Statistics.mean(context.vector("signal"));
            double lowMean = Statistics.mean(context.vector("lowControl"));
            double highMean = Statistics.mean(context.vector("highControl"));
            double range = highMean - lowMean;
            if (range == 0) {
                throw new ArithmeticException("Low and high controls have the same mean; cannot normalise");
            }
            return CalculationResult.of(100.0 * (signal - lowMean) / range);
        }
    }

    /**
     * Z'-factor assay quality metric: {@code 1 - 3*(sdPos + sdNeg) / |meanPos - meanNeg|}.
     */
    public static final class ZPrime implements CalculationProvider {
        @Override
        public String id() {
            return LibraryCalculations.Z_PRIME;
        }

        @Override
        public CalculationResult evaluate(CalculationContext context, @Nullable JsonNode params) {
            double[] pos = context.vector("positiveControl");
            double[] neg = context.vector("negativeControl");
            double separation = Math.abs(Statistics.mean(pos) - Statistics.mean(neg));
            if (separation == 0) {
                throw new ArithmeticException("Control means are equal; Z'-factor is undefined");
            }
            return CalculationResult.of(1.0 - 3.0 * (Statistics.stdDev(pos) + Statistics.stdDev(neg)) / separation);
        }
    }
}
