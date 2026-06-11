package com.epam.indigoeln.assay.service.calc;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registry of built-in {@link CalculationProvider}s plus the entry point for custom-formula
 * evaluation. Both the library calculations and the formula evaluator are pure functions of
 * their inputs, so a derived value can always be recomputed deterministically from its sources.
 */
@ApplicationScoped
public class CalculationLibrary {

    private final Map<String, CalculationProvider> providers = new LinkedHashMap<>();

    public CalculationLibrary() {
        register(new BasicStatProviders.Mean());
        register(new BasicStatProviders.StandardDeviation());
        register(new BasicStatProviders.CoefficientOfVariation());
        register(new NormalizationProviders.PercentInhibition());
        register(new NormalizationProviders.NormalizeToControls());
        register(new NormalizationProviders.ZPrime());
        register(new Ic50CurveFitProvider());
    }

    private void register(CalculationProvider provider) {
        providers.put(provider.id(), provider);
    }

    public Set<String> availableLibraryIds() {
        return Set.copyOf(providers.keySet());
    }

    /** Evaluates a built-in library calculation by id. */
    public CalculationResult evaluateLibrary(String libraryId, CalculationContext context, @Nullable JsonNode params) {
        CalculationProvider provider = providers.get(libraryId);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown library calculation: " + libraryId);
        }
        return provider.evaluate(context, params);
    }

    /** Evaluates a custom formula against named numeric inputs. */
    public CalculationResult evaluateFormula(String expression, Map<String, double[]> inputs) {
        return CalculationResult.of(FormulaEvaluator.evaluate(expression, inputs));
    }

    /** Validates a custom formula's syntax without data; throws {@link FormulaException} if invalid. */
    public void validateFormula(String expression) {
        FormulaEvaluator.validate(expression);
    }

    public List<String> libraryIds() {
        return List.copyOf(providers.keySet());
    }
}
