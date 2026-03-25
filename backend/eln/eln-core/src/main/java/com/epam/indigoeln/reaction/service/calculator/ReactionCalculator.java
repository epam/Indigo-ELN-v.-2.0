package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.eln.util.ExperimentModelUtil;
import com.epam.indigoeln.reaction.metamodel.ReactionInputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionInputSampleMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputMetamodel;
import com.epam.indigoeln.reaction.metamodel.ReactionOutputSampleMetamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.ZERO_MOL;
import static com.epam.indigoeln.reaction.model.units.EnteredValueOpt.opt;
import static com.epam.indigoeln.reaction.model.units.EnteredValueSource.DEFAULT;

@Slf4j
@ApplicationScoped
public class ReactionCalculator {

    private final List<Conflict> conflicts = new ArrayList<>();
    private final List<Conflict> overwrittenConflicts = new ArrayList<>();

    public void recalculate(ExperimentModel model) {
        for (;;) {
            ExperimentModelUtil.prepareToRecalculate(model);
            conflicts.clear();
            recalculateModel(model);
            if (conflicts.isEmpty()) {
                break;
            }
            Conflict conflictToOverwrite = StreamEx.of(conflicts)
                    .minBy(x -> {
                        EnteredValue<NoUnit> value = x.property.get(x.container);
                        return value != null && value.getSource().isUserEntered() ? value.getSource().getPriority() : Integer.MAX_VALUE;
                    })
                    .orElseThrow();
            conflictToOverwrite.property.set(conflictToOverwrite.container, null);
            overwrittenConflicts.add(conflictToOverwrite);
        }
        // set "overwritten" flag
        for (Conflict conflict : overwrittenConflicts) {
            EnteredValue<NoUnit> value = conflict.property.get(conflict.container);
            if (value != null) {
                log.info("Overwritten {} because of a calculation conflict", conflict.property.name());
                value.setOverwritten(true);
            }
        }
    }

    private void recalculateModel(ExperimentModel model) {
        updateCycle("reaction", () -> {
            boolean updated = false;
            for (Reaction reaction : model.getReactions()) {
                ReactionInput limitingInput = reaction.getLimitingInput();
                if (limitingInput == null) {
                    continue; // empty reaction
                }
                updated |= recalculateInput(limitingInput);
                for (ReactionInput input : reaction.getInputs()) {
                    if (input != limitingInput) {
                        updated |= recalculateInput(input);
                    }
                }
                for (ReactionOutput output : reaction.getOutputs()) {
                    updated |= recalculateOutput(output);
                }
            }
            return updated;
        });
    }

    private boolean recalculateInput(ReactionInput input) {
        EnteredValue<MolWeightUnit> molWeight = input.getCompound().getMolWeight();
        return updateCycle("input " + input.getAnchor(), () -> {
            boolean updated = false;
            EnteredValueOpt<MolUnit> mol = opt(ZERO_MOL);
            for (ReactionInputSample sample : input.getSamples()) {
                mol = mol.add(sample.getMol());
            }
            EnteredValueOpt<MolUnit> mol2 = opt(null);
            EnteredValueOpt<NoUnit> eq = opt(null);
            if (!input.isLimiting()) {
                ReactionInput limitingInput = input.getReaction().getLimitingInput();
                if (limitingInput != null) {
                    mol2 = opt(limitingInput.getMol()).divide(limitingInput.getEq()).multiply(input.getEq());
                    eq = opt(input.getMol()).divide(limitingInput.getMol()).multiply(limitingInput.getEq());
                }
            }
            updated |= tryUpdate(
                    "input.mol", input, ReactionInputMetamodel.MOL,
                    // molCompound = sum(molSamples)
                    mol,
                    // molCompound = molCompoundLimiting / eqLimiting * eq
                    mol2
            );
            updated |= tryUpdate(
                    "input.eq", input, ReactionInputMetamodel.EQ,
                    eq
            );
            for (ReactionInputSample sample : input.getSamples()) {
                updated |= recalculateInputSample(sample, molWeight);
            }
            return updated;
        });
    }

    private boolean recalculateInputSample(ReactionInputSample sample, @Nullable EnteredValue<MolWeightUnit> molWeight) {
        return updateCycle("input sample " + sample.getAnchor(), () -> {
            boolean updated = false;
            EnteredValueOpt<MolUnit> rowMol = opt(sample.getRow().getMol());
            EnteredValueOpt<MolUnit> otherSamplesMol = StreamEx.of(sample.getRow().getSamples())
                    .filter(s -> s != sample)
                    .map(s -> EnteredValueOpt.opt(s.getMol()))
                    .reduce(EnteredValueOpt.opt(ZERO_MOL), EnteredValueOpt::add);
            updated |= tryUpdate(
                    "inputSample.mol", sample, ReactionInputSampleMetamodel.MOL,
                    // mol = molCompound - sum(molOtherSamples)
                    rowMol.subtract(otherSamplesMol),
                    // mol = weight * purity / molWeight
                    opt(sample.getWeight()).multiply(sample.getPurityAsFraction()).divide(molWeight),
                    // mol = molarity * volume
                    opt(sample.getMolarity()).multiply(sample.getVolume())
            );
            updated |= tryUpdate(
                    "inputSample.weight", sample, ReactionInputSampleMetamodel.WEIGHT,
                    // weight = mol * molWeight / purity * 100
                    opt(sample.getMol()).multiply(molWeight).multiply(100.0).divide(sample.getPurity()),
                    // weight = volume * density
                    opt(sample.getVolume()).multiply(sample.getDensity())
            );
            updated |= tryUpdate(
                    "inputSample.volume", sample, ReactionInputSampleMetamodel.VOLUME,
                    // volume = weight / density
                    opt(sample.getWeight()).divide(sample.getDensity()),
                    // volume = mol / molarity
                    opt(sample.getMol()).divide(sample.getMolarity())
            );
            return updated;
        });
    }

    private boolean recalculateOutput(ReactionOutput output) {
        EnteredValue<MolWeightUnit> molWeight = output.getCompound().getMolWeight();
        return updateCycle("output " + output.getAnchor(), () -> {
            boolean updated = false;
            EnteredValueOpt<MolUnit> theoMol = opt(null);
            EnteredValueOpt<NoUnit> eq = opt(null);
            ReactionInput limitingInput = output.getReaction().getLimitingInput();
            if (limitingInput != null) {
                theoMol = opt(limitingInput.getMol()).divide(limitingInput.getEq()).multiply(output.getEq());
                eq = opt(limitingInput.getMol()).divide(limitingInput.getEq()).divide(output.getTheoMol());
            }
            updated |= tryUpdate(
                    "output.theoMol", output, ReactionOutputMetamodel.THEO_MOL,
                    // theoMol = molInputCompoundLimiting / eqInputCompoundLimiting * eq
                    theoMol
            );
            updated |= tryUpdate(
                    "output.eq", output, ReactionOutputMetamodel.EQ,
                    eq
            );
            updated |= tryUpdate(
                    "output.theoWeight", output, ReactionOutputMetamodel.THEO_WEIGHT,
                    opt(output.getTheoMol()).multiply(molWeight)
            );
            for (ReactionOutputSample sample : output.getSamples()) {
                updated |= recalculateOutputSample(sample, molWeight);
            }
            return updated;
        });
    }

    private boolean recalculateOutputSample(ReactionOutputSample sample, @Nullable EnteredValue<MolWeightUnit> molWeight) {
        return updateCycle("output sample " + sample.getAnchor(), () -> {
            boolean updated = false;
            updated |= tryUpdate(
                    "outputSample.actualMol", sample, ReactionOutputSampleMetamodel.ACTUAL_MOL,
                    // actualMol = actualWeight * purity / molWeight
                    opt(sample.getActualWeight()).multiply(sample.getPurityAsFraction()).divide(molWeight),
                    // actualMol = molarity * volume
                    opt(sample.getMolarity()).multiply(sample.getVolume())
            );
            updated |= tryUpdate(
                    "outputSample.actualWeight", sample, ReactionOutputSampleMetamodel.ACTUAL_WEIGHT,
                    // actualWeight = actualMol * molWeight / purity
                    opt(sample.getActualMol()).<WeightUnit>multiply(molWeight).divide(sample.getPurityAsFraction()),
                    // actualWeight = volume * density
                    opt(sample.getVolume()).multiply(sample.getDensity())
            );
            updated |= tryUpdate(
                    "outputSample.volume", sample, ReactionOutputSampleMetamodel.VOLUME,
                    // volume = actualWeight / density
                    opt(sample.getActualWeight()).divide(sample.getDensity()),
                    // volume = actualMol / molarity
                    opt(sample.getActualMol()).divide(sample.getMolarity())
            );
            updated |= tryUpdate(
                    "outputSample.yield", sample, ReactionOutputSampleMetamodel.YIELD,
                    // yield = actualMol / theoMol
                    opt(sample.getActualMol()).<NoUnit>divide(sample.getRow().getTheoMol()).multiply(100.0),
                    // yield = actualWeight * purity / theoWeight
                    opt(sample.getActualWeight()).multiply(sample.getPurityAsFraction()).<NoUnit>divide(sample.getRow().getTheoWeight()).multiply(100.0)
            );
            return updated;
        });
    }

    @SafeVarargs
    private <C, R extends MeasurementUnit> boolean tryUpdate(String displayName, C container, ModelProperty<C, EnteredValue<R>, ?, ?> property, EnteredValueOpt<R>... results) {
        boolean updated = false;
        EnteredValue<R> targetCurrent = property.get(container);
//        boolean checkConflicts = false;
        for (EnteredValueOpt<R> result : results) {
            if (result.getValue() != null) {
                if (targetCurrent == null) {
                    log.debug("tryUpdate: {}: no previous value, set to {}", displayName, result.getValue());
                    property.set(container, result.getValue().cast());
                    updated = true;
                } else {
                    boolean resultIsMorePriority = result.getValue().getSource().getPriority() > targetCurrent.getSource().getPriority();
                    if (targetCurrent.valueEquals(result.getValue())) {
                        // reassign value to reflect priority change, but don't consider it an update because value didn't change
                        if (resultIsMorePriority) {
                            log.debug("tryUpdate: {}: same value recalculated, but source changed from {} to {}", displayName, targetCurrent.getSource(), result.getValue().getSource());
                            property.set(container, result.getValue());
                        }
                    } else if (targetCurrent.getSource() == DEFAULT && resultIsMorePriority) {
                        // reassign default value
                        log.debug("tryUpdate: {}: default value {} overwritten to {}", displayName, targetCurrent, result.getValue());
                        property.set(container, result.getValue());
                    } else {
                        // values don't match, meaning a conflict; if new value has more priority, update to new value
                        log.debug("tryUpdate: {}: conflict, current value {}, new value {}", displayName, targetCurrent, result.getValue());
                        if (resultIsMorePriority) {
                            property.set(container, result.getValue());
                            targetCurrent = result.getValue();
                            updated = true;
                        }
                        conflicts.add(new Conflict(container, property.cast()));
                    }
                }
            }
        }
        return updated;
    }

    private boolean updateCycle(String displayName, BooleanSupplier block) {
        log.debug("updateCycle: started: {}", displayName);
        boolean anyUpdates = false;
        boolean lastUpdated = true;
        while (lastUpdated) {
            lastUpdated = block.getAsBoolean();
            anyUpdates |= lastUpdated;
            log.debug("updateCycle: iteration done: {}", displayName);
        }
        log.debug("updateCycle: complete{}: {}", anyUpdates ? " with updates" : " without updates", displayName);
        return anyUpdates;
    }

    private record Conflict (
            Object container,
            ModelProperty<Object, EnteredValue<NoUnit>, Object, Object> property
    ) {}
}
