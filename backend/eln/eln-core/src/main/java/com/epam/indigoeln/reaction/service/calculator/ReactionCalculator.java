package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.reaction.metamodel.ReactionInputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionInputSampleMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputSampleMetamodel;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.enterprise.context.Dependent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.hibernate.internal.util.collections.IdentitySet;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.epam.indigoeln.reaction.service.calculator.EnteredValueOpt.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Slf4j
@Dependent
public class ReactionCalculator {

    private static final Comparator<Property<?, ?>> CONFLICT_SELECTOR = Comparator.comparing(ReactionCalculator::conflictResolutionScore);

    private ModelProps model;

    @Nullable
    private Property<?, ?> overwritten = null;

    public void recalculate(ExperimentModel experimentModel) {
        model = new ModelProps(experimentModel);
        try {
            model.prepareToRecalculate();
            recalculateModel(model);
        } catch (RecalculationConflictException e) {
            log.info("Conflict at {}: values {} vs {}, possible targets are: {}", e.property, e.conflictValue1, e.conflictValue2, e.possibleTargets);
            for (Property<?, ?> target : e.possibleTargets) {
                EnteredValue<?> previousValue = target.getValue();
                overwritten = target;
                try {
                    model.prepareToRecalculate();
                    recalculateModel(model);
                    log.info("Overwritten {}, conflict resolved", target);
                    return;
                } catch (RecalculationConflictException e2) {
                    log.info("Tried {}, conflict NOT resolved", target);
                    overwritten = null;
                    //noinspection unchecked,rawtypes
                    target.setValue((EnteredValueOpt) opt(previousValue));
                    // continue with the next target
                }
            }
            throw new RecalculationException("Cannot resolve calculation conflicts");
        }
    }

    private void recalculateModel(ModelProps model) {
        updateCycle(model, () -> {
            boolean updated = false;
            for (ReactionProps reaction : model.reactions) {
                InputProps limitingInput = reaction.limitingInput;
                updated |= recalculateInput(reaction, limitingInput);
                for (InputProps input : reaction.inputs) {
                    if (input != limitingInput) {
                        updated |= recalculateInput(reaction, input);
                    }
                }
                for (OutputProps output : reaction.outputs) {
                    updated |= recalculateOutput(reaction, output);
                }
            }
            return updated;
        });
    }

    private boolean recalculateInput(ReactionProps reaction, InputProps input) {
        EnteredValueOpt<MolWeightUnit> molWeight = opt(input.container.getCompound().getMolWeight());
        return updateCycle(input, () -> {
            boolean updated = false;
            EnteredValueOpt<MolUnit> molFromSamples = ZERO_MOL;
            for (InputSampleProps sample : input.samples) {
                molFromSamples = molFromSamples.add(sample.mol);
            }
            EnteredValueOpt<MolUnit> molFromLimiting = empty();
            InputProps limitingInput = reaction.limitingInput;
            boolean isLimiting = input == limitingInput;
            if (!isLimiting) {
                molFromLimiting = limitingInput.mol.divide(limitingInput.eq).multiply(input.eq);
            }
            updated |= tryUpdate(
                    input.mol,
                    // molCompound = sum(molSamples)
                    molFromSamples,
                    // molCompound = molCompoundLimiting / eqLimiting * eq
                    molFromLimiting
            );
            if (!isLimiting) {
                EnteredValueOpt<NoUnit> eq = input.mol.divide(limitingInput.mol).multiply(limitingInput.eq);
                updated |= tryUpdate(
                        input.eq,
                        eq
                );
            } else {
                //noinspection unchecked
                updated |= tryUpdate(
                        input.eq,
                        StreamEx.of(reaction.inputs)
                                .filter(other -> other != input)
                                // eqLimiting = molLimiting * eqNonLimiting / molNonLimiting
                                .map(other -> input.mol.multiply(other.eq).divide(other.mol))
                                .toArray(EnteredValueOpt[]::new)
                );
            }
            for (InputSampleProps sample : input.samples) {
                updated |= recalculateInputSample(input, sample, molWeight);
            }
            return updated;
        });
    }

    private boolean recalculateInputSample(InputProps row, InputSampleProps sample, EnteredValueOpt<MolWeightUnit> molWeight) {
        return updateCycle(sample, () -> {
            boolean updated = false;
            EnteredValueOpt<MolUnit> otherSamplesMol = sum(StreamEx.of(row.samples.iterator())
                    .filter(s -> s != sample)
                    .map(s -> s.mol)
            );
            updated |= tryUpdate(
                    sample.mol,
                    // mol = molCompound - sum(molOtherSamples)
                    row.mol.subtract(otherSamplesMol),
                    // mol = weight * purity / molWeight
                    sample.weight.multiply(sample.purityAsFraction()).divide(molWeight),
                    // mol = molarity * volume
                    sample.molarity.multiply(sample.volume)
            );
            updated |= tryUpdate(
                    sample.weight,
                    // weight = mol * molWeight / purity
                    sample.mol.multiply(molWeight).divide(sample.purityAsFraction()),
                    // weight = volume * density
                    sample.volume.multiply(sample.density)
            );
            updated |= tryUpdate(
                    sample.volume,
                    // volume = weight / density
                    sample.weight.divide(sample.density),
                    // volume = mol / molarity
                    sample.mol.divide(sample.molarity)
            );
            return updated;
        });
    }

    private boolean recalculateOutput(ReactionProps reaction, OutputProps output) {
        EnteredValueOpt<MolWeightUnit> molWeight = opt(output.container.getCompound().getMolWeight());
        return updateCycle(output, () -> {
            boolean updated = false;
            InputProps limitingInput = reaction.limitingInput;
            EnteredValueOpt<MolUnit> theoMol = limitingInput.mol.divide(limitingInput.eq).multiply(output.eq);
            EnteredValueOpt<NoUnit> eq = limitingInput.mol.divide(limitingInput.eq).divide(output.theoMol);
            updated |= tryUpdate(
                    output.theoMol,
                    // theoMol = molInputCompoundLimiting / eqInputCompoundLimiting * eq
                    theoMol
            );
            updated |= tryUpdate(
                    output.eq,
                    eq
            );
            updated |= tryUpdate(
                    output.theoWeight,
                    output.theoMol.multiply(molWeight)
            );
            for (OutputSampleProps sample : output.samples) {
                updated |= recalculateOutputSample(output, sample, molWeight);
            }
            return updated;
        });
    }

    private boolean recalculateOutputSample(OutputProps row, OutputSampleProps sample, EnteredValueOpt<MolWeightUnit> molWeight) {
        return updateCycle(sample, () -> {
            boolean updated = false;
            updated |= tryUpdate(
                    sample.actualMol,
                    // actualMol = actualWeight * purity / molWeight
                    sample.actualWeight.multiply(sample.purityAsFraction()).divide(molWeight),
                    // actualMol = molarity * volume
                    sample.molarity.multiply(sample.volume)
            );
            updated |= tryUpdate(
                    sample.actualWeight,
                    // actualWeight = actualMol * molWeight / purity
                    sample.actualMol.<WeightUnit>multiply(molWeight).divide(sample.purityAsFraction()),
                    // actualWeight = volume * density
                    sample.volume.multiply(sample.density)
            );
            updated |= tryUpdate(
                    sample.volume,
                    // volume = actualWeight / density
                    sample.actualWeight.divide(sample.density),
                    // volume = actualMol / molarity
                    sample.actualMol.divide(sample.molarity)
            );
            updated |= tryUpdate(
                    sample.yield,
                    // yield = actualMol / theoMol
                    sample.actualMol.<NoUnit>divide(row.theoMol).multiply(DEFAULT_ONE_HUNDRED),
                    // yield = actualWeight * purity / theoWeight
                    sample.actualWeight.multiply(sample.purityAsFraction()).<NoUnit>divide(row.theoWeight).multiply(DEFAULT_ONE_HUNDRED)
            );
            return updated;
        });
    }

    @SafeVarargs
    private <C extends ExperimentNode, R extends MeasurementUnit> boolean tryUpdate(Property<C, R> value, EnteredValueOpt<R>... results) {
        boolean updated = false;
        EnteredValue<R> targetCurrent = value.getValue();
        EnteredValue<R> conflictValue1 = null, conflictValue2 = null;
        for (EnteredValueOpt<R> result : results) {
            if (result.getValue() != null) {
                if (targetCurrent == null) {
                    log.debug("tryUpdate: {}: no previous value, set to {}", value.getName(), result.getValue());
                    value.setValue(result);
                    targetCurrent = result.getValue();
                    updated = true;
                    continue;
                }
                if (targetCurrent.valueEquals(result.getValue())) {
                    continue;
                }
                conflictValue1 = targetCurrent;
                conflictValue2 = result.getValue();
            }
        }
        if (conflictValue1 != null && conflictValue2 != null) {
            InputCollector collector = new InputCollector();
            value.collectInputs(collector);
            for (EnteredValueOpt<R> result : results) {
                if (result.getValue() != null) {
                    result.collectInputs(collector);
                }
            }
            log.debug("tryUpdate: conflict at {}, candidates to overwrite: {}", value.getName(), collector.getInputs());
            List<Property<?, ?>> possibleTargets = collector.getInputs().stream()
                    .sorted(CONFLICT_SELECTOR)
                    .toList();
            checkState(!possibleTargets.isEmpty(), "No targets to overwrite");
            throw new RecalculationConflictException(value, conflictValue1, conflictValue2, possibleTargets);
        }
        return updated;
    }

    private boolean updateCycle(AbstractProps object, BooleanSupplier block) {
        log.debug("updateCycle: started: {}", object);
        boolean anyUpdates = false;
        boolean lastUpdated = true;
        while (lastUpdated) {
            lastUpdated = block.getAsBoolean();
            anyUpdates |= lastUpdated;
            log.debug("updateCycle: iteration done: {}", object);
        }
        log.debug("updateCycle: complete{}: {}", anyUpdates ? " with updates" : " without updates", object);
        return anyUpdates;
    }

    private void doPrepareToRecalculate(Property<?, ?> evp) {
        EnteredValue<?> value = evp.getValue();
        if (value != null) {
            if (value.getSource().isCalculated()) {
                evp.reset(false);
            } else if (overwritten == evp) {
                evp.reset(true);
            }
        }
    }

    @RequiredArgsConstructor
    private static class RecalculationConflictException extends RuntimeException {

        private final EnteredValueOpt.Property<?, ?> property;
        private final EnteredValue<?> conflictValue1;
        private final EnteredValue<?> conflictValue2;
        private final List<Property<?, ?>> possibleTargets;
    }

    public static class RecalculationException extends RuntimeException {

        public RecalculationException(String message) {
            super(message);
        }
    }

    private static class InputCollector implements Consumer<@Nullable EnteredValueOpt<?>> {

        @Getter
        private final Set<Property<?, ?>> inputs = new IdentitySet<>();
        private final Set<EnteredValueOpt<?>> visited = new IdentitySet<>();

        @Override
        public void accept(@Nullable EnteredValueOpt<?> ev) {
            if (ev != null) {
                if (visited.add(ev)) {
                    if (ev instanceof EnteredValueOpt.Property<?, ?> evp && evp.isCanOverwrite()) {
                        EnteredValue<?> value = evp.getValue();
                        if (value != null && (value.getSource().isUserEntered() || value.getSource().isDefault())) {
                            inputs.add(evp);
                        }
                    }
                    ev.collectInputs(this);
                }
            }
        }
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
        }

        void prepareToRecalculate() {
            for (ReactionProps reaction : reactions) {
                reaction.prepareToRecalculate();
            }
        }
    }

    class ReactionProps extends AbstractProps<Reaction> {

        final List<InputProps> inputs;
        final InputProps limitingInput;
        final List<OutputProps> outputs;

        private ReactionProps(Reaction reaction) {
            super(reaction);
            inputs = StreamEx.of(reaction.getInputs())
                    .map(x -> new InputProps(x))
                    .toList();
            limitingInput = StreamEx.of(inputs)
                    .filter(x -> x.container.isLimiting())
                    .findAny().orElseThrow();
            outputs = StreamEx.of(reaction.getOutputs())
                    .map(x -> new OutputProps(x))
                    .toList();
        }

        void prepareToRecalculate() {
            for (InputProps input : inputs) {
                input.prepareToRecalculate();
            }
            for (OutputProps output : outputs) {
                output.prepareToRecalculate();
            }
        }
    }

    class InputProps extends AbstractProps<ReactionInput> {

        final Property<ReactionInput, NoUnit> eq;
        final Property<ReactionInput, MolUnit> mol;
        final List<InputSampleProps> samples;

        private InputProps(ReactionInput input) {
            super(input);
            eq = prop(input, ReactionInputMetamodel.EQ, true);
            mol = prop(input, ReactionInputMetamodel.MOL, true);
            samples = StreamEx.of(input.getSamples())
                    .map(x -> new InputSampleProps(x))
                    .toList();
        }

        void prepareToRecalculate() {
            doPrepareToRecalculate(eq);
            doPrepareToRecalculate(mol);
            for (InputSampleProps sample : samples) {
                sample.prepareToRecalculate();
            }
        }
    }

    class InputSampleProps extends AbstractProps<ReactionInputSample> {

        final Property<ReactionInputSample, MolUnit> mol;
        final Property<ReactionInputSample, WeightUnit> weight;
        final Property<ReactionInputSample, NoUnit> purity;
        final Property<ReactionInputSample, MolarityUnit> molarity;
        final Property<ReactionInputSample, VolumeUnit> volume;
        final Property<ReactionInputSample, DensityUnit> density;

        EnteredValueOpt<NoUnit> purityAsFraction() {
            return purity.multiply(ONE_HUNDREDTH);
        }

        private InputSampleProps(ReactionInputSample sample) {
            super(sample);
            mol = prop(sample, ReactionInputSampleMetamodel.MOL, true);
            weight = prop(sample, ReactionInputSampleMetamodel.WEIGHT, true);
            purity = prop(sample, ReactionInputSampleMetamodel.PURITY, false);
            molarity = prop(sample, ReactionInputSampleMetamodel.MOLARITY, true);
            volume = prop(sample, ReactionInputSampleMetamodel.VOLUME, true);
            density = prop(sample, ReactionInputSampleMetamodel.DENSITY, true);
        }

        void prepareToRecalculate() {
            doPrepareToRecalculate(mol);
            doPrepareToRecalculate(weight);
            doPrepareToRecalculate(purity);
            doPrepareToRecalculate(molarity);
            doPrepareToRecalculate(volume);
            doPrepareToRecalculate(density);
        }
    }

    class OutputProps extends AbstractProps<ReactionOutput> {

        final Property<ReactionOutput, NoUnit> eq;
        final Property<ReactionOutput, MolUnit> theoMol;
        final Property<ReactionOutput, WeightUnit> theoWeight;
        final List<OutputSampleProps> samples;

        private OutputProps(ReactionOutput output) {
            super(output);
            eq = prop(output, ReactionOutputMetamodel.EQ, true);
            theoMol = prop(output, ReactionOutputMetamodel.THEO_MOL, true);
            theoWeight = prop(output, ReactionOutputMetamodel.THEO_WEIGHT, true);
            samples = StreamEx.of(output.getSamples())
                    .map(OutputSampleProps::new)
                    .toList();
        }

        void prepareToRecalculate() {
            doPrepareToRecalculate(eq);
            doPrepareToRecalculate(theoMol);
            doPrepareToRecalculate(theoWeight);
            for (OutputSampleProps sample : samples) {
                sample.prepareToRecalculate();
            }
        }
    }

    class OutputSampleProps extends AbstractProps<ReactionOutputSample> {

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

        private OutputSampleProps(ReactionOutputSample sample) {
            super(sample);
            actualMol = prop(sample, ReactionOutputSampleMetamodel.ACTUAL_MOL, true);
            actualWeight = prop(sample, ReactionOutputSampleMetamodel.ACTUAL_WEIGHT, true);
            purity = prop(sample, ReactionOutputSampleMetamodel.PURITY, false);
            molarity = prop(sample, ReactionOutputSampleMetamodel.MOLARITY, true);
            volume = prop(sample, ReactionOutputSampleMetamodel.VOLUME, true);
            density = prop(sample, ReactionOutputSampleMetamodel.DENSITY, true);
            yield = prop(sample, ReactionOutputSampleMetamodel.YIELD, true);
        }

        public void prepareToRecalculate() {
            doPrepareToRecalculate(actualMol);
            doPrepareToRecalculate(actualWeight);
            doPrepareToRecalculate(purity);
            doPrepareToRecalculate(molarity);
            doPrepareToRecalculate(volume);
            doPrepareToRecalculate(density);
            doPrepareToRecalculate(yield);
        }
    }

    private static boolean isUnderLimitingInput(Property<?, ?> value) {
        return switch (value.getContainer()) {
            case ReactionInput i -> i.isLimiting();
            case ReactionInputSample s -> s.getRow().isLimiting();
            default -> false;
        };
    }

    private static long conflictResolutionScore(Property<?, ?> x) {
        EnteredValue<?> value = checkNotNull(x.getValue());
        checkState(!value.getSource().isCalculated());
        int priority1; // 0 - default, 1 - user-entered
        int priority2 = isUnderLimitingInput(x) ? 1 : 0;
        int priority3;
        if (value.getSource().isDefault()) {
            priority1 = 0;
            priority3 = x.getName().hashCode(); // make stable choice between defaults when performing redo
        } else if (value.getSource().isUserEntered()) {
            priority1 = 1;
            priority3 = value.getSource().getPriority(); // older edits is less valuable
        } else {
            throw new IllegalArgumentException(value.getSource().toString());
        }
        return (((long) priority1) << 33) | (((long) priority2) << 32) | ((long) priority3);
    }
}
