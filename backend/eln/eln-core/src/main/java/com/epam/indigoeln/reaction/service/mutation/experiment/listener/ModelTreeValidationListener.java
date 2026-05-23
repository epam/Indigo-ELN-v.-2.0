package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.service.mutation.ExperimentModelMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;

@Dependent
@Priority(ExperimentModelMutationListener.VALIDATION_PRIORITY)
public class ModelTreeValidationListener implements ExperimentModelMutationListener {

    @Override
    public void afterRecalculate(ExperimentEntity experiment, ExperimentModel model, ExperimentMutationContext context) {
        try {
            doValidate(model);
        } catch (Exception e) {
            throw new RuntimeException("Mutation produced invalid model: " + e.getMessage(), e);
        }
    }

    private static void doValidate(ExperimentModel model) {
        // validate all parent links are correct
        // validate all anchors are unique
        Set<Anchor> anchors = new HashSet<>();
        for (Reaction reaction : model.getReactions()) {
            checkState(reaction.getModel() == model);
            checkState(anchors.add(reaction.getAnchor()));
            for (ReactionInput input : reaction.getInputs()) {
                checkState(input.getReaction() == reaction);
                checkState(anchors.add(input.getAnchor()));
                for (ReactionInputSample sample : input.getSamples()) {
                    checkState(sample.getRow() == input);
                    checkState(anchors.add(sample.getAnchor()));
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                checkState(output.getReaction() == reaction);
                checkState(anchors.add(output.getAnchor()));
                for (ReactionOutputSample sample : output.getSamples()) {
                    checkState(sample.getRow() == output);
                    checkState(anchors.add(sample.getAnchor()));
                }
            }
        }
        // validate rxnPositions are unique
        for (Reaction reaction : model.getReactions()) {
            Set<Pair<ReactionRole, Integer>> inputPositions = new HashSet<>();
            for (ReactionInput input : reaction.getInputs()) {
                if (input.getRxnPosition() != null) {
                    checkState(inputPositions.add(Pair.of(input.getRole(), input.getRxnPosition())));
                }
            }
            Set<Integer> outputPositions = new HashSet<>();
            for (ReactionOutput output : reaction.getOutputs()) {
                if (output.getRxnPosition() != null) {
                    checkState(outputPositions.add(output.getRxnPosition()));
                }
            }
        }
        // validate input and output compounds are unique
        for (Reaction reaction : model.getReactions()) {
            Set<UUID> inputCompoundIDs = new HashSet<>();
            for (ReactionInput input : reaction.getInputs()) {
                if (input.getCompound().getCompoundID() != null) {
                    checkState(inputCompoundIDs.add(input.getCompound().getCompoundID()));
                }
            }
            Set<UUID> outputCompoundIDs = new HashSet<>();
            for (ReactionOutput output : reaction.getOutputs()) {
                if (output.getCompound().getCompoundID() != null) {
                    checkState(outputCompoundIDs.add(output.getCompound().getCompoundID()));
                }
            }
        }
    }
}
