package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.reaction.metamodel.ReactionInputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionInputSampleMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputSampleMetamodel;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.*;
import com.epam.indigoeln.reaction.service.EnteredValueOpt;
import jakarta.enterprise.context.Dependent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.hibernate.internal.util.collections.IdentitySet;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.epam.indigoeln.reaction.service.EnteredValueOpt.*;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@Dependent
public class ReactionCalculator {

    private ModelProps model;
    private final Set<EnteredValueOpt.Property<?, ?>> overwrittenConflicts = new HashSet<>();

    public void recalculate(ExperimentModel modelObj) {
        model = new ModelProps(modelObj);
        for (;;) {
            try {
                model.prepareToRecalculate();
                recalculateModel(model);
                break;
            } catch (RecalculateException e) {
                // continue
            }
        }
        for (Property<?, ?> value : overwrittenConflicts) {
            if (value.getValue() != null) {
                value.getValue().setOverwritten(true);
            }
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
        EnteredValueOpt<MolWeightUnit> molWeight = opt(input.input.getCompound().getMolWeight());
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
        EnteredValueOpt<MolWeightUnit> molWeight = opt(output.output.getCompound().getMolWeight());
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
    private <C extends ExperimentNode, R extends MeasurementUnit> boolean tryUpdate(EnteredValueOpt.Property<C, R> value, EnteredValueOpt<R>... results) {
        boolean updated = false;
        EnteredValue<R> targetCurrent = value.getValue();
        boolean hasConflict = false;
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
                hasConflict = true;
            }
        }
        if (hasConflict) {
            InputCollector collector = new InputCollector();
            value.collectInputs(collector);
            for (EnteredValueOpt<R> result : results) {
                if (result.getValue() != null) {
                    result.collectInputs(collector);
                }
            }
            log.debug("tryUpdate: conflict at {}, candidates to overwrite: {}", value.getName(), collector.getInputs());
            EnteredValueOpt.Property<?, ?> chosen = collector.getInputs().stream()
                    .min(Comparator.<EnteredValueOpt.Property<?, ?>, Integer>comparing(x -> checkNotNull(x.getValue()).getSource().getPriority())
                            .thenComparing(x -> x.getContainer() instanceof ReactionInput i && i.isLimiting() ? 1 : 0))
                    .orElseThrow(() -> new RuntimeException("Cannot find a value to resolve conflict"));
            log.debug("overwrite input value to resolve conflict: {}", chosen);
            chosen.setValue(empty());
            overwrittenConflicts.add(chosen);
            throw new RecalculateException();
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

    private void doPrepareToRecalculate(EnteredValueOpt.Property<?, ?> evp) {
        EnteredValue<?> value = evp.getValue();
        if (value != null) {
            if (value.getSource().isCalculated()) {
                evp.reset(false);
            } else if (value.getSource().isDefault() && overwrittenConflicts.contains(evp)) {
                evp.reset(true);
            }
            value.setOverwritten(false);
        }
    }

    private static class RecalculateException extends RuntimeException {
    }

    private static class InputCollector implements Consumer<@Nullable EnteredValueOpt<?>> {

        @Getter
        private final Set<EnteredValueOpt.Property<?, ?>> inputs = new IdentitySet<>();
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
    static abstract class AbstractProps {

        protected final String displayName;

        @Override
        public String toString() {
            return displayName;
        }
    }

    class ModelProps extends AbstractProps {

        final ExperimentModel model;
        final List<ReactionProps> reactions;

        ModelProps(ExperimentModel model) {
            super("Model");
            this.model = model;
            reactions = StreamEx.of(model.getReactions())
                    .filter(x -> x.getLimitingInput() != null)
                    .map(ReactionProps::new)
                    .toList();
        }

        void prepareToRecalculate() {
            for (ReactionProps reaction : reactions) {
                reaction.prepareToRecalculate();
            }
        }
    }

    class ReactionProps extends AbstractProps {

        final Reaction reaction;
        final List<InputProps> inputs;
        final InputProps limitingInput;
        final List<OutputProps> outputs;

        private ReactionProps(Reaction reaction) {
            super(log.isDebugEnabled() ? "Reaction " + (reaction.getModel().getReactions().indexOf(reaction) + 1) : "");
            this.reaction = reaction;
            inputs = StreamEx.of(reaction.getInputs())
                    .map(x -> new InputProps(this, x))
                    .toList();
            limitingInput = StreamEx.of(inputs)
                    .filter(x -> x.input.isLimiting())
                    .findAny().orElseThrow();
            outputs = StreamEx.of(reaction.getOutputs())
                    .map(x -> new OutputProps(this, x))
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

    class InputProps extends AbstractProps {

        final ReactionInput input;
        final EnteredValueOpt.Property<ReactionInput, NoUnit> eq;
        final EnteredValueOpt.Property<ReactionInput, MolUnit> mol;
        final List<InputSampleProps> samples;

        private InputProps(ReactionProps reaction, ReactionInput input) {
            super(log.isDebugEnabled() ? reaction.displayName + "/Input " + (input.getReaction().getInputs().indexOf(input) + 1) : "");
            this.input = input;
            eq = prop(input, ReactionInputMetamodel.EQ, true);
            mol = prop(input, ReactionInputMetamodel.MOL, true);
            samples = StreamEx.of(input.getSamples())
                    .map(x -> new InputSampleProps(this, x))
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

    class InputSampleProps extends AbstractProps {

        final ReactionInputSample sample;
        final EnteredValueOpt.Property<ReactionInputSample, MolUnit> mol;
        final EnteredValueOpt.Property<ReactionInputSample, WeightUnit> weight;
        final EnteredValueOpt.Property<ReactionInputSample, NoUnit> purity;
        final EnteredValueOpt.Property<ReactionInputSample, MolarityUnit> molarity;
        final EnteredValueOpt.Property<ReactionInputSample, VolumeUnit> volume;
        final EnteredValueOpt.Property<ReactionInputSample, DensityUnit> density;

        EnteredValueOpt<NoUnit> purityAsFraction() {
            return purity.multiply(ONE_HUNDREDTH);
        }

        private InputSampleProps(InputProps input, ReactionInputSample sample) {
            super(log.isDebugEnabled() ? input.displayName + "/Sample " + (sample.getRow().getSamples().indexOf(sample) + 1) : "");
            this.sample = sample;
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

    class OutputProps extends AbstractProps {

        final ReactionOutput output;
        final EnteredValueOpt.Property<ReactionOutput, NoUnit> eq;
        final EnteredValueOpt.Property<ReactionOutput, MolUnit> theoMol;
        final EnteredValueOpt.Property<ReactionOutput, WeightUnit> theoWeight;
        final List<OutputSampleProps> samples;

        private OutputProps(ReactionProps reaction, ReactionOutput output) {
            super(log.isDebugEnabled() ? reaction.displayName + "/Output " + (output.getReaction().getOutputs().indexOf(output) + 1) : "");
            this.output = output;
            eq = prop(output, ReactionOutputMetamodel.EQ, true);
            theoMol = prop(output, ReactionOutputMetamodel.THEO_MOL, true);
            theoWeight = prop(output, ReactionOutputMetamodel.THEO_WEIGHT, true);
            samples = StreamEx.of(output.getSamples())
                    .map(x -> new OutputSampleProps(this, x))
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

    class OutputSampleProps extends AbstractProps {

        final ReactionOutputSample sample;
        final EnteredValueOpt.Property<ReactionOutputSample, MolUnit> actualMol;
        final EnteredValueOpt.Property<ReactionOutputSample, WeightUnit> actualWeight;
        final EnteredValueOpt.Property<ReactionOutputSample, NoUnit> purity;
        final EnteredValueOpt.Property<ReactionOutputSample, MolarityUnit> molarity;
        final EnteredValueOpt.Property<ReactionOutputSample, VolumeUnit> volume;
        final EnteredValueOpt.Property<ReactionOutputSample, DensityUnit> density;
        final EnteredValueOpt.Property<ReactionOutputSample, NoUnit> yield;

        EnteredValueOpt<NoUnit> purityAsFraction() {
            return purity.multiply(ONE_HUNDREDTH);
        }

        private OutputSampleProps(OutputProps output, ReactionOutputSample sample) {
            super(log.isDebugEnabled() ? output.displayName + "/Sample " + (sample.getRow().getSamples().indexOf(sample) + 1) : "");
            this.sample = sample;
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
}
