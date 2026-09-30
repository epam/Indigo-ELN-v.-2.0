package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.google.common.base.Preconditions.checkState;

@Dependent
@Priority(ExperimentMutationListener.VALIDATION_PRIORITY)
public class ModelTreeValidationListener implements ExperimentMutationListener {

    @Override
    public void afterRecalculate(ExperimentEntity experiment, ExperimentMutationContext context) {
        // anchors of created objects come from the client, so a clash is a bad request
        validateAnchors(experiment.getModel());
        try {
            doValidate(experiment.getModel());
        } catch (Exception e) {
            throw new RuntimeException("Mutation produced invalid model: " + e.getMessage(), e);
        }
    }

    private static void validateAnchors(ExperimentModel model) {
        Set<Anchor> anchors = new HashSet<>();
        for (Reaction reaction : model.getReactions()) {
            validateAnchor(anchors, reaction.getAnchor());
            for (ReactionInput input : reaction.getInputs()) {
                validateAnchor(anchors, input.getAnchor());
                for (ReactionInputSample sample : input.getSamples()) {
                    validateAnchor(anchors, sample.getAnchor());
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                validateAnchor(anchors, output.getAnchor());
                for (ReactionOutputSample sample : output.getSamples()) {
                    validateAnchor(anchors, sample.getAnchor());
                }
            }
        }
    }

    private static void validateAnchor(Set<Anchor> anchors, Anchor anchor) {
        validate(anchors.add(anchor), "Duplicate anchor " + anchor);
    }

    private static void doValidate(ExperimentModel model) {
        // validate all parent links are correct
        for (Reaction reaction : model.getReactions()) {
            checkState(reaction.getModel() == model);
            for (ReactionInput input : reaction.getInputs()) {
                checkState(input.getReaction() == reaction);
                for (ReactionInputSample sample : input.getSamples()) {
                    checkState(sample.getRow() == input);
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                checkState(output.getReaction() == reaction);
                for (ReactionOutputSample sample : output.getSamples()) {
                    checkState(sample.getRow() == output);
                }
            }
        }
        // validate rxnPositions are unique
        for (Reaction reaction : model.getReactions()) {
            Set<Pair<ReactionRole, @Nullable Integer>> inputPositions = new HashSet<>();
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
