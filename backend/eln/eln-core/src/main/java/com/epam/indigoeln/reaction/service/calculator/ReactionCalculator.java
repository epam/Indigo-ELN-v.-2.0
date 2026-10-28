package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.common.model.units.DensityUnit;
import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolWeightUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.epam.indigoeln.reaction.metamodel.ReactionInputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionInputSampleMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputSampleMetamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentNode;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.util.CalculationContext;
import jakarta.enterprise.context.Dependent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.epam.indigoeln.common.model.units.DensityUnit.G_ML;
import static com.epam.indigoeln.common.model.units.MolUnit.MOL;
import static com.epam.indigoeln.common.model.units.NoUnit.NO_UNIT;
import static com.epam.indigoeln.common.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.common.model.units.WeightUnit.G;
import static com.epam.indigoeln.reaction.model.EnteredValue.DEFAULT_ONE;
import static com.epam.indigoeln.reaction.service.calculator.Calculation.readOnly;
import static com.epam.indigoeln.reaction.service.calculator.Calculation.sum;
import static com.google.common.base.Preconditions.checkState;

/**
 * Calculates reaction based on system of equations:
 *
 * <p>F1.1. input.mol = ∑ sample.mol</p>
 * <p>&emsp; F1.2. sample.mol = input.mol - ∑ otherSample.mol</p>
 *
 * <p>F2.1. nonLimiting.mol = limiting.mol / limiting.eq * nonLimiting.eq</p>
 * <p>&emsp; F2.2. nonLimiting.eq = nonLimiting.mol / limiting.mol * limiting.eq</p>
 * <p>&emsp; <s>F2.3. limiting.eq = limiting.mol * nonLimiting.eq / nonLimiting.mol</s> <i>don't calculate limiting from non-limiting</i></p>
 * <p>&emsp; limiting.mol is never calculated from nonLimiting.mol</p>
 *
 * <p>F3.1. sample.mol = sample.weight * sample.purity / molWeight</p>
 * <p>&emsp; F3.2. sample.weight = sample.mol * molWeight / sample.purity</p>
 * <p>&emsp; F3.3. sample.actualMol = sample.actualWeight * sample.purity / molWeight</p>
 * <p>&emsp; F3.4. sample.actualWeight = sample.actualMol * molWeight / sample.purity</p>
 * <p>&emsp; purity is never calculated</p>
 * <p>&emsp; molWeight is never calculated</p>
 *
 * <p>F4.1. sample.mol = sample.molarity * sample.volume</p>
 * <p>&emsp; <s>F4.2. sample.molarity = sample.mol / sample.volume</s></p>
 * <p>&emsp; F4.3. sample.volume = sample.mol / sample.molarity</p>
 * <p>&emsp; F4.4. sample.actualMol = sample.molarity * sample.volume</p>
 * <p>&emsp; <s>F4.5. sample.molarity = sample.actualMol / sample.volume</s></p>
 * <p>&emsp; F4.6. sample.volume = sample.actualMol / sample.molarity</p>
 * <p>&emsp; molarity is never calculated</p>
 *
 * <p>F5.1. sample.weight = sample.volume * sample.density</p>
 * <p>&emsp; F5.2. sample.volume = sample.weight / sample.density</p>
 * <p>&emsp; <s>F5.3. sample.density = sample.weight / sample.volume</s></p>
 * <p>&emsp; F5.4. sample.actualWeight = sample.volume * sample.density</p>
 * <p>&emsp; F5.5. sample.volume = sample.actualWeight / sample.density</p>
 * <p>&emsp; <s>F5.6. sample.density = sample.actualWeight / sample.volume</s></p>
 * <p>&emsp; density is never calculated</p>
 *
 * <p>F6.1. output.theoMol = limiting.mol / limiting.eq * output.eq</p>
 * <p>&emsp; it's the only way to determine theoMol, so cannot calculate others based on theoMol</p>
 *
 * <p>F7.1. output.theoWeight = output.theoMol * molWeight</p>
 * <p>&emsp; it's the only way to determine theoWeight, so cannot calculate others based on theoWeight</p>
 *
 * <p>F8.1. outputSample.yield = outputSample.actualMol / output.theoMol</p>
 * <p>&emsp; F8.2. outputSample.actualMol = outputSample.yield * output.theoMol</p>
 * <p>&emsp; theoMol is only determined from limiting.mol</p>
 *
 * <p>F9.1. outputSample.yield = outputSample.actualWeight * outputSample.purity / output.theoWeight</p>
 * <p>&emsp; F9.2. outputSample.actualWeight = outputSample.yield / outputSample.purity * output.theoWeight</p>
 * <p>&emsp; purity is never calculated</p>
 *
 * <p>F10.1. input.weight = ∑ sample.weight</p>
 * <p>&emsp; it's the only way to determine input.weight, so cannot calculate others based on input.weight</p>
 *
 * <p>F11.1. input.volume = ∑ sample.volume</p>
 * <p>&emsp; it's the only way to determine input.volume, so cannot calculate others based on input.volume</p>
 */
@Slf4j
@Dependent
public class ReactionCalculator {

    private static final Comparator<Seed> SEED_COMPARATOR = ((Comparator<Seed>) ReactionCalculator::compareProperties).reversed();

    private int nodeOrdinal = 0;
    private final List<CalculableProperty<?, ?>> properties = new ArrayList<>();
    private final List<Formula<?>> formulas = new ArrayList<>();
    private final List<Seed> seeds = new ArrayList<>();
    /** Field ids ({@link CalculableProperty#getFieldId()}) of user-entered values reverted to resolve a conflict, to the conflict. */
    @Getter
    private final Map<String, String> conflicts = new LinkedHashMap<>();

    public void recalculate(ExperimentModel experimentModel) {
        log.debug("recalculation started");
        new ModelProps(experimentModel);

        // collect seeds
        CalculableProperty<?, ?> clearedProperty = null;
        for (CalculableProperty<?, ?> property : properties) {
            EnteredValue<?> value = property.get();
            if (value.isUserEntered()) {
                seeds.add(new Seed(property, value));
            } else if (value.isUserCleared()) {
                clearedProperty = property;
                property.setValue(EnteredValue.empty(), null);
            } else if (value.isCalculated()) {
                property.setValue(EnteredValue.empty(), null);
            }
        }

        seeds.sort(SEED_COMPARATOR);
        if (log.isDebugEnabled()) {
            log.debug("seeds:\n\t{}", StreamEx.of(seeds).joining("\n\t"));
        }

        for (Seed seed : seeds) {
            if (!seed.property.get().isFixed()) {
                seed.property.setValue(EnteredValue.empty(), null);
            }
        }

        for (Seed pair : seeds) {
            snapshot();
            try {
                CalculableProperty<?, ?> seed = pair.property;
                EnteredValue<?> seedValue = pair.value;
                log.debug("apply seed: {} = {}", seed.getName(), seedValue);
                if (seed.get().hasValue()) {
                    if (seedValue.isDefault()) {
                        log.debug("value already set to {}, ignoring default", seed);
                        continue;
                    }
                    if (!seed.get().valueEquals(seedValue)) {
                        String message = "%s: seed value conflict:\n\tcurrent : %s (base value %s)\n\tprevious: %s (base value %s) from %s".formatted(seed.getName(), seedValue, seedValue.getBaseValue(), seed, seed.get().getBaseValue(), seed.getCalculatedFrom());
                        throw reportConflict(message);
                    }
                }
                //noinspection unchecked,rawtypes
                seed.setValue((EnteredValue) seedValue, null);
                Deque<Formula<?>> queue = new ArrayDeque<>(seed.getDownstream());
                // walk the graph
                while (!queue.isEmpty()) {
                    Formula<?> formula = queue.removeFirst();
                    if (recalculateFormula(formula)) {
                        queue.addAll(formula.target.getDownstream());
                    }
                }
                if (clearedProperty != null && clearedProperty.get().hasValue()) {
                    throw new RecalculationConflictException("to satisfy cleared property " + clearedProperty);
                }
            } catch (RecalculationConflictException e) {
                log.debug("overwritten: {}: {}", pair.property, e.reason);
                revert();
                if (pair.value.isUserEntered()) {
                    conflicts.put(pair.property.getFieldId(), e.reason);
                }
                // proceed with the next seed
            }
        }
        log.debug("recalculation done");
    }

    private <C extends ExperimentNode, U extends MeasurementUnit> CalculableProperty<C, U> prop(C container, ModelProperty<C, EnteredValue<U>> property, U unit) {
        CalculableProperty<C, U> node = new CalculableProperty<>(container, property, unit, ++nodeOrdinal);
        properties.add(node);
        return node;
    }

    private <C extends ExperimentNode, U extends MeasurementUnit> CalculableProperty<C, U> prop(C container, ModelProperty<C, EnteredValue<U>> property, U unit, EnteredValue<U> defaultValue) {
        CalculableProperty<C, U> node = prop(container, property, unit);
        seeds.add(new Seed(node, defaultValue));
        return node;
    }

    private <U extends MeasurementUnit> Formula<U> formula(String name, CalculableProperty<?, U> target, Calculation<U> supplier, CalculableProperty<?, ?>... sources) {
        Formula<U> formula = new Formula<>(name, target, supplier, sources);
        formulas.add(formula);
        return formula;
    }

    private void snapshot() {
        for (CalculableProperty<?, ?> property : properties) {
            property.snapshot();
        }
        for (Formula<?> formula : formulas) {
            formula.snapshot();
        }
    }

    private void revert() {
        for (CalculableProperty<?, ?> property : properties) {
            property.revert();
        }
        for (Formula<?> formula : formulas) {
            formula.revert();
        }
    }

    private <U extends MeasurementUnit> boolean recalculateFormula(Formula<U> formula) {
        if (formula.value.hasValue()) { // already calculated
            return false;
        }
        EnteredValue<U> calculated = formula.supplier.get();
        if (!calculated.hasValue()) {
            return false;
        }
        CalculableProperty<?, U> target = formula.target;
        if (!target.get().hasValue()) {
            log.debug("calculated {} as {} from formula {}", target.getName(), calculated, formula);
            formula.value = calculated;
            target.setValue(calculated, formula);
            return true;
        }
        if (target.get().valueEquals(calculated)) {
            return false;
        }
        String message = "%s: calculated value conflict:\n\tcurrent : %s (base value %s) from %s\n\tprevious: %s (base value %s) from %s".formatted(target.getName(), calculated, calculated.getBaseValue(), formula, target, target.get().getBaseValue(), target.getCalculatedFrom());
        throw reportConflict(message);
    }

    private RecalculationConflictException reportConflict(String message) {
        log.debug(message);
        return new RecalculationConflictException(message);
    }

    @RequiredArgsConstructor
    private static class RecalculationConflictException extends RuntimeException {

        private final String reason;
    }

    @RequiredArgsConstructor
    static abstract class AbstractProps<C extends ExperimentNode> {

        protected final C container;
    }

    class ModelProps extends AbstractProps<ExperimentModel> {

        ModelProps(ExperimentModel model) {
            super(model);
            for (Reaction modelReaction : model.getReactions()) {
                ReactionProps reaction = new  ReactionProps(modelReaction);
                for (InputProps input : reaction.inputs) {
                    input.init();
                    for (InputSampleProps sample : input.samples) {
                        sample.init();
                    }
                }
                for (OutputProps output : reaction.outputs) {
                    output.init();
                    for (OutputSampleProps sample : output.samples) {
                        sample.init();
                    }
                }
            }
        }
    }

    class ReactionProps extends AbstractProps<Reaction> {

        final List<InputProps> inputs;
        @Nullable
        final InputProps limiting;
        final List<OutputProps> outputs;

        private ReactionProps(Reaction reaction) {
            super(reaction);
            inputs = StreamEx.of(reaction.getInputs())
                    .map(x -> new InputProps(this, x))
                    .toList();
            limiting = StreamEx.of(inputs)
                    .filter(x -> x.container.isLimiting())
                    .findAny().orElse(null);
            outputs = StreamEx.of(reaction.getOutputs())
                    .map(x -> new OutputProps(this, x))
                    .toList();
        }
    }

    class InputProps extends AbstractProps<ReactionInput> {

        final ReactionProps reaction;
        final CalculableProperty<ReactionInput, MolUnit> mol;
        final CalculableProperty<ReactionInput, NoUnit> eq;
        final CalculableProperty<ReactionInput, WeightUnit> weight;
        final CalculableProperty<ReactionInput, VolumeUnit> volume;
        final List<InputSampleProps> samples;

        private InputProps(ReactionProps reaction, ReactionInput input) {
            super(input);
            this.reaction = reaction;
            mol = prop(input, ReactionInputMetamodel.MOL, CalculationContext.getMolUnit());
            eq = prop(input, ReactionInputMetamodel.EQ, NO_UNIT, DEFAULT_ONE);
            weight = prop(input, ReactionInputMetamodel.WEIGHT, CalculationContext.getWeightUnit());
            volume = prop(input, ReactionInputMetamodel.VOLUME, CalculationContext.getVolumeUnit());
            samples = StreamEx.of(input.getSamples())
                    .map(x -> new InputSampleProps(this, x))
                    .toList();
        }

        private void init() {
            checkState(reaction.limiting != null);
            List<CalculableProperty<ReactionInputSample, MolUnit>> sampleMols = StreamEx.of(samples)
                    .map(s -> s.mol)
                    .toList();
            List<CalculableProperty<ReactionInputSample, WeightUnit>> sampleWeights = StreamEx.of(samples)
                    .map(s -> s.weight)
                    .toList();
            List<CalculableProperty<ReactionInputSample, VolumeUnit>> sampleVolumes = StreamEx.of(samples)
                    .map(s -> s.volume)
                    .toList();
            InputProps limiting = reaction.limiting;

            formula(
                    "F1.1: mol = sum sampleN.mol",
                    mol,
                    sum(sampleMols, MOL)
            ).addSources(sampleMols);

            formula(
                    "F10.1: input.weight = sum(sampleN.weight)",
                    weight,
                    sum(sampleWeights, G)
            ).addSources(sampleWeights);

            formula(
                    "F11.1: input.volume = sum(sampleN.volume)",
                    volume,
                    sum(sampleVolumes, ML)
            ).addSources(sampleVolumes);

            if (this != limiting) {
                formula(
                        "F2.1: nonLimiting.mol = limiting.mol / limiting.eq * input.eq",
                        mol,
                        limiting.mol.divide(limiting.eq).multiply(eq),
                        limiting.mol, limiting.eq, eq
                );

                formula(
                        "F2.2: nonLimiting.eq = input.mol / limiting.mol * limiting.eq",
                        eq,
                        mol.divide(limiting.mol).multiply(limiting.eq),
                        mol, limiting.mol, limiting.eq
                );
            }
        }
    }

    class InputSampleProps extends AbstractProps<ReactionInputSample> {

        final InputProps input;
        final CalculableProperty<ReactionInputSample, MolUnit> mol;
        final CalculableProperty<ReactionInputSample, WeightUnit> weight;
        final CalculableProperty<ReactionInputSample, NoUnit> purity;
        final CalculableProperty<ReactionInputSample, MolarityUnit> molarity;
        final CalculableProperty<ReactionInputSample, VolumeUnit> volume;
        final CalculableProperty<ReactionInputSample, DensityUnit> density;
        final Calculation<NoUnit> purityAsFraction;

        private InputSampleProps(InputProps input, ReactionInputSample sample) {
            super(sample);
            this.input = input;
            mol = prop(sample, ReactionInputSampleMetamodel.MOL, CalculationContext.getMolUnit());
            weight = prop(sample, ReactionInputSampleMetamodel.WEIGHT, CalculationContext.getWeightUnit());
            purity = prop(sample, ReactionInputSampleMetamodel.PURITY, NO_UNIT, EnteredValue.DEFAULT_ONE_HUNDRED);
            molarity = prop(sample, ReactionInputSampleMetamodel.MOLARITY, CalculationContext.getMolarityUnit());
            volume = prop(sample, ReactionInputSampleMetamodel.VOLUME, CalculationContext.getVolumeUnit());
            density = prop(sample, ReactionInputSampleMetamodel.DENSITY, G_ML);
            purityAsFraction = purity.multiply(0.01);
        }

        private void init() {
            Calculation<MolWeightUnit> molWeight = readOnly(input.container.getCompound().getMolWeight());

            List<CalculableProperty<ReactionInputSample, MolUnit>> otherSampleMols = StreamEx.of(input.samples)
                    .filter(s -> s != this)
                    .map(s -> s.mol)
                    .toList();

            formula(
                    "F1.2: mol = input.mol - sum(otherSampleN.mol)",
                    mol,
                    input.mol.subtract(sum(otherSampleMols, MOL)),
                    input.mol
            ).addSources(otherSampleMols);

            formula(
                    "F3.1: sample.mol = sample.weight * sample.purity / molWeight",
                    mol,
                    weight.multiply(purityAsFraction).divide(molWeight),
                    weight, purity
            );

            formula(
                    "F4.1: sample.mol = sample.molarity * sample.volume",
                    mol,
                    molarity.multiply(volume),
                    molarity, volume
            );

            formula(
                    "F3.2: sample.weight = sample.mol * molWeight / sample.purity",
                    weight,
                    mol.multiply(molWeight).divide(purityAsFraction),
                    mol, purity
            );

            formula(
                    "F5.1: sample.weight = sample.volume * sample.density",
                    weight,
                    volume.multiply(density),
                    volume, density
            );

            formula(
                    "F4.3: sample.volume = sample.mol / sample.molarity",
                    volume,
                    mol.divide(molarity),
                    mol, molarity
            );

            formula(
                    "F5.2: sample.volume = sample.weight / sample.density",
                    volume,
                    weight.divide(density),
                    weight, density
            );
        }
    }

    class OutputProps extends AbstractProps<ReactionOutput> {

        final ReactionProps reaction;
        final CalculableProperty<ReactionOutput, MolUnit> theoMol;
        final CalculableProperty<ReactionOutput, NoUnit> eq;
        final CalculableProperty<ReactionOutput, WeightUnit> theoWeight;
        final List<OutputSampleProps> samples;

        private OutputProps(ReactionProps reaction, ReactionOutput output) {
            super(output);
            this.reaction = reaction;
            theoMol = prop(output, ReactionOutputMetamodel.THEO_MOL, CalculationContext.getMolUnit());
            eq = prop(output, ReactionOutputMetamodel.EQ, NO_UNIT, DEFAULT_ONE);
            theoWeight = prop(output, ReactionOutputMetamodel.THEO_WEIGHT, CalculationContext.getWeightUnit());
            samples = StreamEx.of(output.getSamples())
                    .map(x -> new OutputSampleProps(this, x))
                    .toList();
        }

        private void init() {
            Calculation<MolWeightUnit> molWeight = readOnly(container.getCompound().getMolWeight());
            InputProps limiting = reaction.limiting;

            if (limiting != null) {
                formula(
                        "F6.1: output.theoMol = limiting.mol / limiting.eq * output.eq",
                        theoMol,
                        limiting.mol.divide(limiting.eq).multiply(eq),
                        limiting.mol, limiting.eq, eq
                );
            }

            formula(
                    "F7.1: output.theoWeight = output.theoMol * molWeight",
                    theoWeight,
                    theoMol.multiply(molWeight),
                    theoMol
            );
        }
    }

    class OutputSampleProps extends AbstractProps<ReactionOutputSample> {

        final OutputProps output;
        final CalculableProperty<ReactionOutputSample, MolUnit> actualMol;
        final CalculableProperty<ReactionOutputSample, WeightUnit> actualWeight;
        final CalculableProperty<ReactionOutputSample, NoUnit> purity;
        final CalculableProperty<ReactionOutputSample, MolarityUnit> molarity;
        final CalculableProperty<ReactionOutputSample, VolumeUnit> volume;
        final CalculableProperty<ReactionOutputSample, DensityUnit> density;
        final CalculableProperty<ReactionOutputSample, NoUnit> yieldValue;
        final Calculation<NoUnit> purityAsFraction;
        final Calculation<NoUnit> yieldAsFraction;

        private OutputSampleProps(OutputProps output, ReactionOutputSample sample) {
            super(sample);
            this.output = output;
            actualMol = prop(sample, ReactionOutputSampleMetamodel.ACTUAL_MOL, CalculationContext.getMolUnit());
            actualWeight = prop(sample, ReactionOutputSampleMetamodel.ACTUAL_WEIGHT, CalculationContext.getWeightUnit());
            purity = prop(sample, ReactionOutputSampleMetamodel.PURITY, NO_UNIT, EnteredValue.DEFAULT_ONE_HUNDRED);
            molarity = prop(sample, ReactionOutputSampleMetamodel.MOLARITY, CalculationContext.getMolarityUnit());
            volume = prop(sample, ReactionOutputSampleMetamodel.VOLUME, CalculationContext.getVolumeUnit());
            density = prop(sample, ReactionOutputSampleMetamodel.DENSITY, G_ML);
            yieldValue = prop(sample, ReactionOutputSampleMetamodel.YIELD, NO_UNIT);
            purityAsFraction = purity.multiply(0.01);
            yieldAsFraction = yieldValue.multiply(0.01);
        }

        private void init() {
            Calculation<MolWeightUnit> molWeight = readOnly(output.container.getCompound().getMolWeight());

            formula(
                    "F3.3: sample.actualMol = sample.actualWeight * sample.purity / molWeight",
                    actualMol,
                    actualWeight.multiply(purityAsFraction).divide(molWeight),
                    actualWeight, purity
            );

            formula(
                    "F4.4: sample.actualMol = sample.molarity * sample.volume",
                    actualMol,
                    molarity.multiply(volume),
                    molarity, volume
            );

            formula(
                    "F8.2: outputSample.actualMol = outputSample.yield * output.theoMol",
                    actualMol,
                    yieldAsFraction.multiply(output.theoMol),
                    yieldValue, output.theoMol
            );

            formula(
                    "F3.4: sample.actualWeight = sample.actualMol * molWeight / sample.purity",
                    actualWeight,
                    actualMol.multiply(molWeight).divide(purityAsFraction),
                    actualMol, purity
            );

            formula(
                    "F5.4: sample.actualWeight = sample.volume * sample.density",
                    actualWeight,
                    volume.multiply(density),
                    volume, density
            );

            formula(
                    "F9.2: outputSample.actualWeight = outputSample.yield / outputSample.purity * output.theoWeight",
                    actualWeight,
                    yieldAsFraction.divide(purityAsFraction).multiply(output.theoWeight),
                    yieldValue, purity, output.theoWeight
            );

            formula(
                    "F4.6: sample.volume = sample.actualMol / sample.molarity",
                    volume,
                    actualMol.divide(molarity),
                    actualMol, molarity
            );

            formula(
                    "F5.5: sample.volume = sample.actualWeight / sample.density",
                    volume,
                    actualWeight.divide(density),
                    actualWeight, density
            );

            formula(
                    "F8.1: outputSample.yield = outputSample.actualMol / output.theoMol",
                    yieldValue,
                    actualMol.percentageOf(output.theoMol),
                    actualMol, output.theoMol
            );

            formula(
                    "F9.1: outputSample.yield = outputSample.actualWeight * outputSample.purity / output.theoWeight",
                    yieldValue,
                    actualWeight.<WeightUnit>multiply(purityAsFraction).percentageOf(output.theoWeight),
                    actualWeight, purity, output.theoWeight
            );
        }
    }

    private static boolean isUnderLimitingInput(CalculableProperty<?, ?> value) {
        return switch (value.getContainer()) {
            case ReactionInput i -> i.isLimiting();
            case ReactionInputSample s -> s.getRow().isLimiting();
            default -> false;
        };
    }

    private static int compareProperties(Seed pa, Seed pb) {
        CalculableProperty<?, ?> a = pa.property, b = pb.property;
        EnteredValue<?> valueA = pa.value, valueB = pb.value;
        checkState(valueA.isUser() || valueA.isDefault());
        checkState(valueB.isUser() || valueB.isDefault());

        boolean userEnteredA = valueA.isUser();
        boolean userEnteredB = valueB.isUser();

        int result;

        // purity, density and molarity are more priority than other (default purity is more important than other defaults and even other user-entered)
        // (since in case of conflict we can likely recalculate user-entered, but we are not allowed to calculate purity)
        boolean priorityA = a.getModelProperty().name().equals("purity") || a.getModelProperty().name().equals("density") || a.getModelProperty().name().equals("molarity");
        boolean priorityB = b.getModelProperty().name().equals("purity") || b.getModelProperty().name().equals("density") || b.getModelProperty().name().equals("molarity");
        result = Boolean.compare(priorityA, priorityB);
        if (result != 0) {
            return result;
        }

        // values under limiting input is more priority
        boolean limitingA = isUnderLimitingInput(a);
        boolean limitingB = isUnderLimitingInput(b);
        result = Boolean.compare(limitingA, limitingB);
        if (result != 0) {
            return result;
        }

        // user-entered is more priority
        result = Boolean.compare(userEnteredA, userEnteredB);
        if (result != 0) {
            return result;
        }

        // later user-entered is more priority
        if (userEnteredA && userEnteredB) {
            result = Integer.compare(valueA.getPriority(), valueB.getPriority());
            if (result != 0) {
                return result;
            }
        }

        // to have total ordering, use model order as fallback
        return Integer.compare(a.getOrdinal(), b.getOrdinal());
    }

    private record Seed (
            CalculableProperty<?, ?> property,
            EnteredValue<?> value
    ) {}
}
