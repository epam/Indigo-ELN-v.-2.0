package com.epam.indigoeln.assay.service.calc;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class CalculationLibraryTest {

    private final CalculationLibrary library = new CalculationLibrary();

    private CalculationContext ctx(Map<String, double[]> inputs) {
        return new CalculationContext(inputs);
    }

    @Test
    void computesPercentInhibitionRelativeToControls() {
        CalculationContext context = ctx(Map.of(
                "signal", new double[]{20},
                "negativeControl", new double[]{100, 100},
                "positiveControl", new double[]{0, 0}));
        CalculationResult result = library.evaluateLibrary(LibraryCalculations.PERCENT_INHIBITION, context, null);
        // negMean=100, posMean=0, signal=20 -> 100*(100-20)/100 = 80
        assertThat(result.value()).isCloseTo(80.0, within(1e-9));
    }

    @Test
    void computesZPrimeFactor() {
        CalculationContext context = ctx(Map.of(
                "positiveControl", new double[]{100, 102, 98},
                "negativeControl", new double[]{0, 1, -1}));
        CalculationResult result = library.evaluateLibrary(LibraryCalculations.Z_PRIME, context, null);
        assertThat(result.value()).isLessThan(1.0).isGreaterThan(0.0);
    }

    @Test
    void aggregatesWithBuiltInStatistics() {
        CalculationContext context = ctx(Map.of("values", new double[]{10, 20, 30}));
        assertThat(library.evaluateLibrary(LibraryCalculations.MEAN, context, null).value()).isEqualTo(20);
        assertThat(library.evaluateLibrary(LibraryCalculations.SD, context, null).value()).isEqualTo(10);
    }

    @Test
    void evaluatesCustomFormula() {
        CalculationResult result = library.evaluateFormula(
                "100 * (high - signal) / (high - low)",
                Map.of("signal", new double[]{25}, "high", new double[]{100}, "low", new double[]{0}));
        assertThat(result.value()).isCloseTo(75.0, within(1e-9));
    }

    @Test
    void reportsUnknownLibraryCalculation() {
        assertThatThrownBy(() -> library.evaluateLibrary("NO_SUCH_CALC", ctx(Map.of()), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ic50CurveFitIsRegisteredButNotYetImplemented() {
        assertThat(library.availableLibraryIds()).contains(LibraryCalculations.IC50_4PL);
        assertThatThrownBy(() -> library.evaluateLibrary(
                LibraryCalculations.IC50_4PL, ctx(Map.of("dose", new double[]{1}, "response", new double[]{1})), null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
