package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.outputsample.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.EnteredValueOpt;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.ZERO_MOL;
import static com.epam.indigoeln.reaction.model.units.EnteredValueOpt.opt;
import static com.epam.indigoeln.reaction.model.units.EnteredValueSource.DEFAULT;

@Slf4j
@ApplicationScoped
public class ReactionCalculator {

    public void recalculate(ExperimentModel model) {
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
            EnteredValueOpt mol = opt(ZERO_MOL);
            for (ReactionInputSample sample : input.getSamples()) {
                mol = mol.add(sample.getMol());
            }
            EnteredValueOpt mol2 = opt(null);
            EnteredValueOpt eq = opt(null);
            if (!input.isLimiting()) {
                ReactionInput limitingInput = input.getReaction().getLimitingInput();
                if (limitingInput != null) {
                    mol2 = opt(limitingInput.getMol()).divide(limitingInput.getEq()).multiply(input.getEq());
                    eq = opt(input.getMol()).divide(limitingInput.getMol()).multiply(limitingInput.getEq());
                }
            }
            updated |= tryUpdate(
                    "input.mol", input.getMol(), input::setMol,
                    // molCompound = sum(molSamples)
                    mol,
                    // molCompound = molCompoundLimiting / eqLimiting * eq
                    mol2
            );
            updated |= tryUpdate(
                    "input.eq", input.getEq(), input::setEq,
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
            EnteredValueOpt mol = opt(sample.getRow().getMol());
            for (ReactionInputSample otherSample : sample.getRow().getSamples()) {
                if (otherSample != sample) {
                    mol = mol.subtract(otherSample.getMol());
                }
            }
            updated |= tryUpdate(
                    "inputSample.mol", sample.getMol(), sample::setMol,
                    // mol = molCompound - sum(molOtherSamples)
                    mol,
                    // mol = weight * purity / molWeight
                    opt(sample.getWeight()).multiply(sample.getPurity()).divide(molWeight),
                    // mol = molarity * volume
                    opt(sample.getMolarity()).multiply(sample.getVolume())
            );
            updated |= tryUpdate(
                    "inputSample.weight", sample.getWeight(), sample::setWeight,
                    // weight = mol * molWeight / purity
                    opt(sample.getMol()).multiply(molWeight).divide(sample.getPurity()),
                    // weight = volume * density
                    opt(sample.getVolume()).multiply(sample.getDensity())
            );
            updated |= tryUpdate(
                    "inputSample.volume", sample.getVolume(), sample::setVolume,
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
            EnteredValueOpt theoMol = opt(null);
            EnteredValueOpt eq = opt(null);
            ReactionInput limitingInput = output.getReaction().getLimitingInput();
            if (limitingInput != null) {
                theoMol = opt(limitingInput.getMol()).divide(limitingInput.getEq()).multiply(output.getEq());
                eq = opt(limitingInput.getMol()).divide(limitingInput.getEq()).divide(output.getTheoMol());
            }
            updated |= tryUpdate(
                    "output.theoMol", output.getTheoMol(), output::setTheoMol,
                    // theoMol = molInputCompoundLimiting / eqInputCompoundLimiting * eq
                    theoMol
            );
            updated |= tryUpdate(
                    "output.eq", output.getEq(), output::setEq,
                    eq
            );
            updated |= tryUpdate(
                    "output.theoWeight", output.getTheoWeight(), output::setTheoWeight,
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
                    "outputSample.actualMol", sample.getActualMol(), sample::setActualMol,
                    // actualMol = actualWeight * purity / molWeight
                    opt(sample.getActualWeight()).multiply(sample.getPurity()).divide(molWeight),
                    // actualMol = molarity * volume
                    opt(sample.getMolarity()).multiply(sample.getVolume())
            );
            updated |= tryUpdate(
                    "outputSample.actualWeight", sample.getActualWeight(), sample::setActualWeight,
                    // actualWeight = actualMol * molWeight / purity
                    opt(sample.getActualMol()).multiply(molWeight).divide(sample.getPurity()),
                    // actualWeight = volume * density
                    opt(sample.getVolume()).multiply(sample.getDensity())
            );
            updated |= tryUpdate(
                    "outputSample.volume", sample.getVolume(), sample::setVolume,
                    // volume = actualWeight / density
                    opt(sample.getActualWeight()).divide(sample.getDensity()),
                    // volume = actualMol / molarity
                    opt(sample.getActualMol()).divide(sample.getMolarity())
            );
            updated |= tryUpdate(
                    "outputSample.yield", sample.getYield(), sample::setYield,
                    // yield = actualMol / theoMol
                    opt(sample.getActualMol()).divide(sample.getRow().getTheoMol()),
                    // yield = actualWeight / theoWeight
                    opt(sample.getActualWeight()).multiply(sample.getPurity()).divide(sample.getRow().getTheoWeight())
            );
            return updated;
        });
    }

    private <R extends MeasurementUnit> boolean tryUpdate(String displayName, @Nullable EnteredValue<R> targetCurrent, Consumer<EnteredValue<R>> targetSetter, EnteredValueOpt... results) {
        boolean updated = false;
        EnteredValue<R> original = targetCurrent;
//        boolean checkConflicts = false;
        for (EnteredValueOpt result : results) {
            if (result.getValue() != null) {
                if (targetCurrent == null) {
                    log.debug("tryUpdate: {}: no previous value, set to {}", displayName, result.getValue());
                    targetSetter.accept(result.getValue().cast());
                    updated = true;
                } else {
                    boolean resultIsMorePriority = result.getValue().getSource().ordinal() < (targetCurrent.getSource().ordinal());
                    if (targetCurrent.valueEquals(result.getValue())) {
                        // reassign value to reflect priority change, but don't consider it an update because value didn't change
                        if (resultIsMorePriority) {
//                            log.debug("tryUpdate: {}: same value recalculated, but source changed from {} to {}", displayName, targetCurrent.getSource(), result.getValue().getSource());
//                            targetSetter.accept(result.getValue().cast());
                        }
                    } else if (targetCurrent.getSource() == DEFAULT && resultIsMorePriority) {
                        // reassign default value
                        log.debug("tryUpdate: {}: default value {} overwritten to {}", displayName, targetCurrent, result.getValue());
                        targetSetter.accept(result.getValue().cast());
                    } else {
                        // values don't match, meaning a conflict; if new value has more priority, update to new value
                        log.debug("tryUpdate: {}: conflict, current value {}, new value {}", displayName, targetCurrent, result.getValue());
                        if (resultIsMorePriority) {
                            targetSetter.accept(result.getValue().cast());
                            targetCurrent = result.getValue().cast();
                            updated = true;
                        }
                        targetCurrent.setConflict(true);
                    }
                }
            }
        }
//        if (checkConflicts) {
//            // consider all current calculated values, plus original value, if it's user entered
//            List<EnteredValue<?>> allValues = new ArrayList<>();
//            if (original != null && original.getSource() != DEFAULT && original.getSource() != CALCULATED && original.getSource() != CALCULATED_FROM_LAST_ENTERED) {
//                allValues.add(original.cast());
//            }
//            for (EnteredValueOpt result : results) {
//                if (result.getValue() != null) {
//                    allValues.add(result.getValue().cast());
//                }
//            }
//            boolean hasDifferentValues = StreamEx.ofPairs(allValues, EnteredValue::valueEquals).anyMatch(x -> !x);
//            if (hasDifferentValues) {
//                EnteredValue<?> priorityValue = allValues.stream()
//                        .min(Comparator.comparing(x -> x.getSource().ordinal()))
//                        .orElseThrow();
//                log.debug("tryUpdate: {}: conflict: candidate values: {}, selected {}", displayName, allValues, priorityValue);
//                targetSetter.accept(priorityValue.cast());
//                priorityValue.setConflict(true);
////                updated = true;
//            } else {
//                log.debug("tryUpdate: {}: no conflict, all values equal: {}", displayName, allValues);
//                targetSetter.accept(allValues.getFirst().cast());
//                allValues.getFirst().setConflict(false);
//            }
//        }
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
}
