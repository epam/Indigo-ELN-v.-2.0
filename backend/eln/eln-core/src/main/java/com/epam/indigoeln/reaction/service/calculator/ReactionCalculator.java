package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.reaction.metamodel.ReactionInputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionInputSampleMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputSampleMetamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.enterprise.context.Dependent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;
import static com.epam.indigoeln.reaction.service.calculator.EnteredValueOpt.*;
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
 * <p>&emsp; F4.2. sample.molarity = sample.mol / sample.volume</p>
 * <p>&emsp; F4.3. sample.volume = sample.mol / sample.molarity</p>
 * <p>&emsp; F4.4. sample.actualMol = sample.molarity * sample.volume</p>
 * <p>&emsp; F4.5. sample.molarity = sample.actualMol / sample.volume</p>
 * <p>&emsp; F4.6. sample.volume = sample.actualMol / sample.molarity</p>
 *
 * <p>F5.1. sample.weight = sample.volume * sample.density</p>
 * <p>&emsp; F5.2. sample.volume = sample.weight / sample.density</p>
 * <p>&emsp; F5.3. sample.density = sample.weight / sample.volume</p>
 * <p>&emsp; F5.4. sample.actualWeight = sample.volume * sample.density</p>
 * <p>&emsp; F5.5. sample.volume = sample.actualWeight / sample.density</p>
 * <p>&emsp; F5.6. sample.density = sample.actualWeight / sample.volume</p>
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
 */
@Slf4j
@Dependent
public class ReactionCalculator {

    private static final Comparator<Pair<Property<?, ?>, EnteredValue<?>>> SEED_COMPARATOR = ((Comparator<Pair<Property<?,?>, EnteredValue<?>>>) ReactionCalculator::compareProperties).reversed();

    private int nodeOrdinal = 0;
    private final List<Property<?, ?>> properties = new ArrayList<>();
    private final List<Formula<?>> formulas = new ArrayList<>();
    private final List<Pair<Property<?, ?>, EnteredValue<?>>> seeds = new ArrayList<>();
    private final List<Property<?, ?>> overwritten = new ArrayList<>();
    @Getter
    private final List<String> debugMessages = new ArrayList<>();

    public void recalculate(ExperimentModel experimentModel) {
        log.debug("recalculation started");
        new ModelProps(experimentModel);

        // collect seeds
        // TODO use sample density/molarity/purity as defaults, for samples from DB
        for (Property<?, ?> property : properties) {
            EnteredValue<?> value = property.getValue();
            if (!value.isEmpty()) {
                if (value.getSource().isUserEntered()) {
                    seeds.add(Pair.of(property, value));
                } else if (value.getSource().isCalculated()) {
                    property.setValue(EnteredValue.empty(), null);
                }
            }
        }

        seeds.sort(SEED_COMPARATOR);
        if (log.isDebugEnabled()) {
            log.debug("seeds:\n\t{}", StreamEx.of(seeds).joining("\n\t"));
        }

        for (Pair<Property<?, ?>, EnteredValue<?>> seed : seeds) {
            if (!seed.a().getValue().getSource().isFixed()) {
                seed.a().setValue(EnteredValue.empty(), null);
            }
        }

        for (Pair<Property<?, ?>, EnteredValue<?>> pair : seeds) {
            snapshot();
            try {
                Property<?, ?> seed = pair.a();
                // set selected value
                EnteredValue<?> existingValue = seed.getValue();
                EnteredValue<?> seedValue = pair.b();
                log.debug("apply seed: {} = {}", seed.getName(), seedValue);
                if (!existingValue.isEmpty()) {
                    if (seedValue.getSource().isDefault()) {
                        log.debug("value already set to {}, ignoring default", existingValue);
                        continue;
                    }
                    if (!existingValue.valueEquals(seedValue)) {
                        String message = "%s: seed value conflict:\n\tcurrent : %s (exact value %s)\n\tprevious: %s (exact value %s) from %s".formatted(seed.getName(), seedValue, seedValue.toExactBigDecimal(), existingValue, existingValue.toExactBigDecimal(), seed.getCalculatedFrom());
                        throw reportConflict(message);
                    }
                }
                seed.setValueUnchecked(seedValue);
                Deque<Formula<?>> queue = new ArrayDeque<>(seed.getDownstream());
                // walk the graph
                while (!queue.isEmpty()) {
                    Formula<?> formula = queue.removeFirst();
                    if (recalculateFormula(formula)) {
                        queue.addAll(formula.target.getDownstream());
                    }
                }
            } catch (RecalculationConflictException e) {
                log.debug("conflict! overwritten: {}", pair.a());
                overwritten.add(pair.a());
                revert();
                // proceed with the next seed
            }
        }
        log.debug("recalculation done");
        if (!overwritten.isEmpty()) {
            log.debug("overwritten: {}", overwritten);
            for (Property<?, ?> property : overwritten) {
                EnteredValue<?> value = property.getValue();
                value = value.withOverwritten(true);
                property.setValueUnchecked(value);
            }
        }
    }

    private <C extends ExperimentNode, U extends MeasurementUnit> EnteredValueOpt.Property<C, U> prop(C container, ModelProperty<C, EnteredValue<U>> property) {
        Property<C, U> node = new Property<>(container, property, ++nodeOrdinal);
        properties.add(node);
        return node;
    }

    private <C extends ExperimentNode, U extends MeasurementUnit> EnteredValueOpt.Property<C, U> prop(C container, ModelProperty<C, EnteredValue<U>> property, EnteredValue<U> defaultValue) {
        Property<C, U> node = prop(container, property);
        seeds.add(Pair.of(node, defaultValue));
        return node;
    }

    private <U extends MeasurementUnit> Formula<U> formula(String name, Property<?, U> target, Supplier<EnteredValueOpt<U>> supplier, Property<?, ?>... sources) {
        Formula<U> formula = new Formula<>(name, target, supplier, sources);
        formulas.add(formula);
        return formula;
    }

    private void snapshot() {
        for (Property<?, ?> property : properties) {
            property.snapshot();
        }
        for (Formula<?> formula : formulas) {
            formula.snapshot();
        }
    }

    private void revert() {
        for (Property<?, ?> property : properties) {
            property.revert();
        }
        for (Formula<?> formula : formulas) {
            formula.revert();
        }
    }

    private <U extends MeasurementUnit> boolean recalculateFormula(Formula<U> formula) {
        if (!formula.value.isEmpty()) {
            return false;
        }
        EnteredValue<U> calculated = formula.supplier.get().getValue();
        if (calculated.isEmpty()) {
            return false;
        }
        Property<?, U> target = formula.target;
        EnteredValue<U> stored = target.getValue();
        if (stored.isEmpty()) {
            log.debug("calculated {} as {} (exact value {}) from formula {}", target.getName(), calculated, calculated.toExactBigDecimal(), formula);
            formula.value = calculated;
            target.setValue(calculated, formula);
            return true;
        }
        if (stored.valueEquals(calculated)) {
            return false;
        }
        String message = "%s: calculated value conflict:\n\tcurrent : %s (exact value %s) from %s\n\tprevious: %s (exact value %s) from %s".formatted(target.getName(), calculated, calculated.toExactBigDecimal(), formula, stored, stored.toExactBigDecimal(), target.getCalculatedFrom());
        throw reportConflict(message);
    }

    private RecalculationConflictException reportConflict(String message) {
        log.debug(message);
        debugMessages.add(message);
        return new RecalculationConflictException();
    }

    private static class RecalculationConflictException extends RuntimeException {
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
        final Property<ReactionInput, MolUnit> mol;
        final Property<ReactionInput, NoUnit> eq;
        final List<InputSampleProps> samples;

        private InputProps(ReactionProps reaction, ReactionInput input) {
            super(input);
            this.reaction = reaction;
            mol = prop(input, ReactionInputMetamodel.MOL);
            eq = prop(input, ReactionInputMetamodel.EQ, DEFAULT_ONE);
            samples = StreamEx.of(input.getSamples())
                    .map(x -> new InputSampleProps(this, x))
                    .toList();
        }

        private void init() {
            checkState(reaction.limiting != null);
            List<Property<ReactionInputSample, MolUnit>> sampleMols = StreamEx.of(samples)
                    .map(s -> s.mol)
                    .toList();
            InputProps limiting = reaction.limiting;

            formula(
                    "F1.1: mol = sum sampleN.mol",
                    mol,
                    () -> EnteredValueOpt.sum(sampleMols)
            ).addSources(sampleMols);

            if (this != limiting) {

                formula(
                        "F2.1: nonLimiting.mol = limiting.mol / limiting.eq * input.eq",
                        mol,
                        () -> limiting.mol.divide(limiting.eq).multiply(eq),
                        limiting.mol, limiting.eq, eq
                );

                formula(
                        "F2.2: nonLimiting.eq = input.mol / limiting.mol * limiting.eq",
                        eq,
                        () -> mol.divide(limiting.mol).multiply(limiting.eq),
                        mol, limiting.mol, limiting.eq
                );
            } else {
//
//                for (InputProps nonLimiting : reaction.inputs) {
//                    if (this != nonLimiting) {
//                        formula(
//                                "F2.3: limiting.eq = limiting.mol * nonLimiting.eq / nonLimiting.mol",
//                                eq,
//                                () -> mol.multiply(nonLimiting.eq).divide(nonLimiting.mol),
//                                mol, nonLimiting.mol, nonLimiting.eq
//                        );
//                    }
//                }
            }
        }
    }

    class InputSampleProps extends AbstractProps<ReactionInputSample> {

        final InputProps input;
        final Property<ReactionInputSample, MolUnit> mol;
        final Property<ReactionInputSample, WeightUnit> weight;
        final Property<ReactionInputSample, NoUnit> purity;
        final Property<ReactionInputSample, MolarityUnit> molarity;
        final Property<ReactionInputSample, VolumeUnit> volume;
        final Property<ReactionInputSample, DensityUnit> density;

        EnteredValueOpt<NoUnit> purityAsFraction() {
            return purity.multiply(ONE_HUNDREDTH);
        }

        private InputSampleProps(InputProps input, ReactionInputSample sample) {
            super(sample);
            this.input = input;
            mol = prop(sample, ReactionInputSampleMetamodel.MOL);
            weight = prop(sample, ReactionInputSampleMetamodel.WEIGHT);
            purity = prop(sample, ReactionInputSampleMetamodel.PURITY, EnteredValue.DEFAULT_ONE_HUNDRED);
            molarity = prop(sample, ReactionInputSampleMetamodel.MOLARITY);
            volume = prop(sample, ReactionInputSampleMetamodel.VOLUME);
            density = prop(sample, ReactionInputSampleMetamodel.DENSITY);
        }

        private void init() {
            EnteredValueOpt<MolWeightUnit> molWeight = opt(input.container.getCompound().getMolWeight());

            List<Property<ReactionInputSample, MolUnit>> otherSampleMols = StreamEx.of(input.samples)
                    .filter(s -> s != this)
                    .map(s -> s.mol)
                    .toList();

            formula(
                    "F1.2: mol = input.mol - ∑ otherSampleN.mol",
                    mol,
                    () -> input.mol.subtract(sum(otherSampleMols)),
                    input.mol
            ).addSources(otherSampleMols);

            formula(
                    "F3.1: sample.mol = sample.weight * sample.purity / molWeight",
                    mol,
                    () -> weight.multiply(purityAsFraction()).divide(molWeight),
                    weight, purity
            );

            formula(
                    "F4.1: sample.mol = sample.molarity * sample.volume",
                    mol,
                    () -> molarity.multiply(volume),
                    molarity, volume
            );

            formula(
                    "F3.2: sample.weight = sample.mol * molWeight / sample.purity",
                    weight,
                    () -> mol.multiply(molWeight).divide(purityAsFraction()),
                    mol, purity
            );

            formula(
                    "F5.1: sample.weight = sample.volume * sample.density",
                    weight,
                    () -> volume.multiply(density),
                    volume, density
            );

            formula(
                    "F4.2: sample.molarity = sample.mol / sample.volume",
                    molarity,
                    () -> mol.divide(volume),
                    mol, volume
            );

            formula(
                    "F4.3: sample.volume = sample.mol / sample.molarity",
                    volume,
                    () -> mol.divide(molarity),
                    mol, molarity
            );

            formula(
                    "F5.2: sample.volume = sample.weight / sample.density",
                    volume,
                    () -> weight.divide(density),
                    weight, density
            );

            formula(
                    "F5.3: sample.density = sample.weight / sample.volume",
                    density,
                    () -> weight.divide(volume),
                    weight, volume
            );
        }
    }

    class OutputProps extends AbstractProps<ReactionOutput> {

        final ReactionProps reaction;
        final Property<ReactionOutput, MolUnit> theoMol;
        final Property<ReactionOutput, NoUnit> eq;
        final Property<ReactionOutput, WeightUnit> theoWeight;
        final List<OutputSampleProps> samples;

        private OutputProps(ReactionProps reaction, ReactionOutput output) {
            super(output);
            this.reaction = reaction;
            theoMol = prop(output, ReactionOutputMetamodel.THEO_MOL);
            eq = prop(output, ReactionOutputMetamodel.EQ, DEFAULT_ONE);
            theoWeight = prop(output, ReactionOutputMetamodel.THEO_WEIGHT);
            samples = StreamEx.of(output.getSamples())
                    .map(x -> new OutputSampleProps(this, x))
                    .toList();
        }

        private void init() {
            EnteredValueOpt<MolWeightUnit> molWeight = opt(container.getCompound().getMolWeight());
            InputProps limiting = reaction.limiting;

            if (limiting != null) {
                formula(
                        "F6.1: output.theoMol = limiting.mol / limiting.eq * output.eq",
                        theoMol,
                        () -> limiting.mol.divide(limiting.eq).multiply(eq),
                        limiting.mol, limiting.eq, eq
                );
            }

            formula(
                    "F7.1: output.theoWeight = output.theoMol * molWeight",
                    theoWeight,
                    () -> theoMol.multiply(molWeight),
                    theoMol
            );
        }
    }

    class OutputSampleProps extends AbstractProps<ReactionOutputSample> {

        final OutputProps output;
        final Property<ReactionOutputSample, MolUnit> actualMol;
        final Property<ReactionOutputSample, WeightUnit> actualWeight;
        final Property<ReactionOutputSample, NoUnit> purity;
        final Property<ReactionOutputSample, MolarityUnit> molarity;
        final Property<ReactionOutputSample, VolumeUnit> volume;
        final Property<ReactionOutputSample, DensityUnit> density;
        final Property<ReactionOutputSample, NoUnit> yield;

        EnteredValueOpt<NoUnit> purityAsFraction() {
            return purity.multiply(ONE_HUNDREDTH);
        }

        private OutputSampleProps(OutputProps output, ReactionOutputSample sample) {
            super(sample);
            this.output = output;
            actualMol = prop(sample, ReactionOutputSampleMetamodel.ACTUAL_MOL);
            actualWeight = prop(sample, ReactionOutputSampleMetamodel.ACTUAL_WEIGHT);
            purity = prop(sample, ReactionOutputSampleMetamodel.PURITY, EnteredValue.DEFAULT_ONE_HUNDRED);
            molarity = prop(sample, ReactionOutputSampleMetamodel.MOLARITY);
            volume = prop(sample, ReactionOutputSampleMetamodel.VOLUME);
            density = prop(sample, ReactionOutputSampleMetamodel.DENSITY);
            yield = prop(sample, ReactionOutputSampleMetamodel.YIELD);
        }

        private void init() {
            EnteredValueOpt<MolWeightUnit> molWeight = opt(output.container.getCompound().getMolWeight());

            formula(
                    "F3.3: sample.actualMol = sample.actualWeight * sample.purity / molWeight",
                    actualMol,
                    () -> actualWeight.multiply(purityAsFraction()).divide(molWeight),
                    actualWeight, purity
            );

            formula(
                    "F4.4: sample.actualMol = sample.molarity * sample.volume",
                    actualMol,
                    () -> molarity.multiply(volume),
                    molarity, volume
            );

            formula(
                    "F8.2: outputSample.actualMol = outputSample.yield * output.theoMol",
                    actualMol,
                    () -> yield.divide(DEFAULT_ONE_HUNDRED).multiply(output.theoMol),
                    yield, output.theoMol
            );

            formula(
                    "F3.4: sample.actualWeight = sample.actualMol * molWeight / sample.purity",
                    actualWeight,
                    () -> actualMol.multiply(molWeight).divide(purityAsFraction()),
                    actualMol, purity
            );

            formula(
                    "F5.4: sample.actualWeight = sample.volume * sample.density",
                    actualWeight,
                    () -> volume.multiply(density),
                    volume, density
            );

            formula(
                    "F9.2: outputSample.actualWeight = outputSample.yield / outputSample.purity * output.theoWeight",
                    actualWeight,
                    () -> yield.multiply(ONE_HUNDREDTH).divide(purityAsFraction()).multiply(output.theoWeight),
                    yield, purity, output.theoWeight
            );

            formula(
                    "F4.5: sample.molarity = sample.actualMol / sample.volume",
                    molarity,
                    () -> actualMol.divide(volume),
                    actualMol, volume
            );

            formula(
                    "F4.6: sample.volume = sample.actualMol / sample.molarity",
                    volume,
                    () -> actualMol.divide(molarity),
                    actualMol, molarity
            );

            formula(
                    "F5.5: sample.volume = sample.actualWeight / sample.density",
                    volume,
                    () -> actualWeight.divide(density),
                    actualWeight, density
            );

            formula(
                    "F5.6: sample.density = sample.actualWeight / sample.volume",
                    density,
                    () -> actualWeight.divide(volume),
                    actualWeight, volume
            );

            formula(
                    "F8.1: outputSample.yield = outputSample.actualMol / output.theoMol",
                    yield,
                    () -> actualMol.divide(output.theoMol).multiply(DEFAULT_ONE_HUNDRED),
                    actualMol, output.theoMol
            );

            formula(
                    "F9.1: outputSample.yield = outputSample.actualWeight * outputSample.purity / output.theoWeight",
                    yield,
                    () -> actualWeight.multiply(purityAsFraction()).divide(output.theoWeight).multiply(DEFAULT_ONE_HUNDRED),
                    actualWeight, purity, output.theoWeight
            );
        }
    }

    private static boolean isUnderLimitingInput(Property<?, ?> value) {
        return switch (value.getContainer()) {
            case ReactionInput i -> i.isLimiting();
            case ReactionInputSample s -> s.getRow().isLimiting();
            default -> false;
        };
    }

    private static int compareProperties(Pair<Property<?, ?>, EnteredValue<?>> pa, Pair<Property<?, ?>, EnteredValue<?>> pb) {
        Property<?, ?> a = pa.a(), b = pb.a();
        EnteredValue<?> valueA = pa.b(), valueB = pb.b();
        EnteredValueSource sourceA = valueA.getSource(), sourceB = valueB.getSource();
        checkState(sourceA.isUserEntered() || sourceA.isDefault());
        checkState(sourceB.isUserEntered() || sourceB.isDefault());

        boolean userEnteredA = sourceA.isUserEntered();
        boolean userEnteredB = sourceB.isUserEntered();

        int result;

        // purity is more priority than other (default purity is more important than other defaults and even other user-entered)
        // (since in case of conflict we can likely recalculate user-entered, but we are not allowed to calculate purity)
        boolean purityA = a.getProperty().name().equals("purity");
        boolean purityB = b.getProperty().name().equals("purity");
        result = Boolean.compare(purityA, purityB);
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
            result = Integer.compare(sourceA.getPriority(), sourceB.getPriority());
            if (result != 0) {
                return result;
            }
        }

        // to have total ordering, use model order as fallback
        return Integer.compare(a.getOrdinal(), b.getOrdinal());
    }
}
