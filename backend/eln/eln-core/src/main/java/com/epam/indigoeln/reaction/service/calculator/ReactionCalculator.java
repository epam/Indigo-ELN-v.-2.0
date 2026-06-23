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

import java.util.*;
import java.util.function.Supplier;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;
import static com.epam.indigoeln.reaction.service.calculator.EnteredValueOpt.*;
import static com.google.common.base.Preconditions.checkState;

/**
 * Calculates reaction based on system of equations:
 *
 * <p>F1. input.mol = ∑ sample.mol</p>
 * <p>F2. nonLimiting.mol = limiting.mol / limiting.eq * nonLimiting.eq</p>
 * <p>F3. sample.mol = sample.weight * sample.purity / molWeight</p>
 * <p>F4. sample.mol = sample.molarity * sample.volume</p>
 * <p>F5. sample.weight = sample.volume * sample.density</p>
 * <p>F6. output.theoMol = limiting.mol / limiting.eq * output.eq</p>
 * <p>F7. output.theoWeight = output.theoMol * molWeight</p>
 * <p>F8. outputSample.yield = outputSample.actualMol / output.theoMol</p>
 * <p>F9. outputSample.yield = outputSample.actualWeight * outputSample.purity / output.theoWeight</p>
 */
@Slf4j
@Dependent
public class ReactionCalculator {

    private static final Comparator<Pair<Property<?, ?>, EnteredValue<?>>> SEED_COMPARATOR = ((Comparator<Pair<Property<?,?>, EnteredValue<?>>>) ReactionCalculator::compareProperties).reversed();

    private int nodeOrdinal = 0;
    private final List<Property<?, ?>> properties = new ArrayList<>();
    private final List<Formula<?>> formulas = new ArrayList<>();
    private final List<Pair<Property<?, ?>, EnteredValue<?>>> seeds = new ArrayList<>();
    @Getter
    private final List<Property<?, ?>> overwritten = new ArrayList<>();

    public void recalculate(ExperimentModel experimentModel) {
        log.debug("recalculation started");
        new ModelProps(experimentModel);

        // collect seeds
        for (Property<?, ?> property : properties) {
            EnteredValue<?> value = property.getValue();
            if (!value.isEmpty()) {
                if (value.getSource().isUserEntered() || value.getSource().isDefault()) {
                    seeds.add(Pair.of(property, value));
                } else if (value.getSource().isCalculated()) {
                    property.setValue(EnteredValue.empty());
                }
            }
        }

        seeds.sort(SEED_COMPARATOR);
        if (log.isDebugEnabled()) {
            log.debug("seeds:\n\t{}", StreamEx.of(seeds).joining("\t\n"));
        }

        for (Pair<Property<?, ?>, EnteredValue<?>> seed : seeds) {
            if (!seed.a().getValue().getSource().isFixed()) {
                seed.a().setValue(EnteredValue.empty());
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
                        throw new RecalculationConflictException();
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
            log.debug("calculated {} to {} from formula {}", target.getName(), calculated, formula);
            formula.value = calculated;
            target.setValue(calculated);
            return true;
        }
        if (stored.valueEquals(calculated)) {
            return false;
        }
        log.debug("calculated value {} from formula {} conflicts with existing value {} for {}", calculated, formula, stored, target.getName());
        throw new RecalculationConflictException();
    }

    private static class RecalculationConflictException extends RuntimeException {
    }

    @RequiredArgsConstructor
    static abstract class AbstractProps<C extends ExperimentNode> {

        protected final C container;
    }

    class ModelProps extends AbstractProps<ExperimentModel> {

        final List<ReactionProps> reactions;

        ModelProps(ExperimentModel model) {
            super(model);
            reactions = StreamEx.of(model.getReactions())
                    .filter(x -> !x.getInputs().isEmpty())
                    .map(ReactionProps::new)
                    .toList();

            for (ReactionProps reaction : reactions) {
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
        final InputProps limiting;
        final List<OutputProps> outputs;

        private ReactionProps(Reaction reaction) {
            super(reaction);
            inputs = StreamEx.of(reaction.getInputs())
                    .map(x -> new InputProps(this, x))
                    .toList();
            limiting = StreamEx.of(inputs)
                    .filter(x -> x.container.isLimiting())
                    .findAny().orElseThrow();
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
            List<Property<ReactionInputSample, MolUnit>> sampleMols = StreamEx.of(samples)
                    .map(s -> s.mol)
                    .toList();
            InputProps limiting = reaction.limiting;

            // F1. mol = ∑ sampleN.mol
            formula(
                    "F1.1",
                    mol,
                    () -> EnteredValueOpt.sum(sampleMols)
            ).addSources(sampleMols);

            if (this != limiting) {

                // F2. mol = limiting.mol / limiting.eq * input.eq
                formula(
                        "F2.1",
                        mol,
                        () -> limiting.mol.divide(limiting.eq).multiply(eq),
                        limiting.mol, limiting.eq, eq
                );

                // F2. eq = input.mol / limiting.mol * limiting.eq
                formula(
                        "F2.2",
                        eq,
                        () -> mol.divide(limiting.mol).multiply(limiting.eq),
                        mol, limiting.mol, limiting.eq
                );
            } else {

                // F2 - never calculate limiting.mol from nonLimiting.mol

                // F2. eq = mol * nonLimiting.eq / nonLimiting.mol
                for (InputProps nonLimiting : reaction.inputs) {
                    if (this != nonLimiting) {
                        formula(
                                "F2.3",
                                eq,
                                () -> mol.multiply(nonLimiting.eq).divide(nonLimiting.mol),
                                mol, nonLimiting.mol, nonLimiting.eq
                        );
                    }
                }
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

            // F1. mol = input.mol - ∑ otherSampleN.mol
            formula(
                    "F1.2",
                    mol,
                    () -> input.mol.subtract(sum(otherSampleMols)),
                    input.mol
            ).addSources(otherSampleMols);

            // F3. sample.mol = sample.weight * sample.purity / molWeight
            formula(
                    "F3.1",
                    mol,
                    () -> weight.multiply(purityAsFraction()).divide(molWeight),
                    weight, purity
            );

            // F4. sample.mol = sample.molarity * sample.volume
            formula(
                    "F4.1",
                    mol,
                    () -> molarity.multiply(volume),
                    molarity, volume
            );

            // F3. sample.weight = sample.mol * molWeight / sample.purity
            formula(
                    "F3.2",
                    weight,
                    () -> mol.multiply(molWeight).divide(purityAsFraction()),
                    mol, purity
            );

            // F5. sample.weight = sample.volume * sample.density
            formula(
                    "F5.1",
                    weight,
                    () -> volume.multiply(density),
                    volume, density
            );

            // purity is not calculated (can be either default or user-entered)

            // F4. sample.molarity = sample.mol / sample.volume
            formula(
                    "F4.2",
                    molarity,
                    () -> mol.divide(volume),
                    mol, volume
            );

            // F4. sample.volume = sample.mol / sample.molarity
            formula(
                    "F4.3",
                    volume,
                    () -> mol.divide(molarity),
                    mol, molarity
            );

            // F5. sample.volume = sample.weight / sample.density
            formula(
                    "F5.2",
                    volume,
                    () -> weight.divide(density),
                    weight, density
            );

            // F5. sample.density = sample.weight / sample.volume
            formula(
                    "F5.3",
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

            // F6. output.theoMol = limiting.mol / limiting.eq * output.eq
            formula(
                    "F6.1",
                    theoMol,
                    () -> limiting.mol.divide(limiting.eq).multiply(eq),
                    limiting.mol, limiting.eq, eq
            );

            // F6. output.eq = limiting.mol / limiting.eq / output.theoMol
            formula(
                    "F6.2",
                    eq,
                    () -> limiting.mol.divide(limiting.eq).divide(theoMol),
                    limiting.mol, limiting.eq, theoMol
            );

            // F7. output.theoWeight = output.theoMol * molWeight
            formula(
                    "F7.1",
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

            // F3. sample.actualMol = sample.actualWeight * sample.purity / molWeight
            formula(
                    "F3.3",
                    actualMol,
                    () -> actualWeight.multiply(purityAsFraction()).divide(molWeight),
                    actualWeight, purity
            );

            // F4. sample.actualMol = sample.molarity * sample.volume
            formula(
                    "F4.4",
                    actualMol,
                    () -> molarity.multiply(volume),
                    molarity, volume
            );

            // F3. sample.actualWeight = sample.actualMol * molWeight / sample.purity
            formula(
                    "F3.4",
                    actualWeight,
                    () -> actualMol.multiply(molWeight).divide(purityAsFraction()),
                    actualMol, purity
            );

            // F5. sample.actualWeight = sample.volume * sample.density
            formula(
                    "F5.4",
                    actualWeight,
                    () -> volume.multiply(density),
                    volume, density
            );

            // purity is not calculated (can be either default or user-entered)

            // F4. sample.molarity = sample.actualMol / sample.volume
            formula(
                    "F4.5",
                    molarity,
                    () -> actualMol.divide(volume),
                    actualMol, volume
            );

            // F4. sample.volume = sample.actualMol / sample.molarity
            formula(
                    "F4.6",
                    volume,
                    () -> actualMol.divide(molarity),
                    actualMol, molarity
            );

            // F5. sample.volume = sample.actualWeight / sample.density
            formula(
                    "F5.5",
                    volume,
                    () -> actualWeight.divide(density),
                    actualWeight, density
            );

            // F5. sample.density = sample.actualWeight / sample.volume
            formula(
                    "F5.6",
                    density,
                    () -> actualWeight.divide(volume),
                    actualWeight, volume
            );

            // F8. outputSample.yield = outputSample.actualMol / output.theoMol
            formula(
                    "F8",
                    yield,
                    () -> actualMol.divide(output.theoMol).multiply(DEFAULT_ONE_HUNDRED),
                    actualMol, output.theoMol
            );

            // F9. outputSample.yield = outputSample.actualWeight * outputSample.purity / output.theoWeight
            formula(
                    "F9.1",
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

        // user-entered is more priority
        result = Boolean.compare(userEnteredA, userEnteredB);
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
