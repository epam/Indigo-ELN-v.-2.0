package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.outputsample.ReactionOutputSample;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ExperimentModel implements ExperimentModelNode, ToStringTree {

    @Valid
    @NotEmpty
    @JsonManagedReference
    private List<Reaction> reactions;

    public Reaction locate(ReactionMutation mutation) {
        return locateReaction(mutation.anchor());
    }

    public Reaction locateReaction(UUID anchor) {
        for (Reaction reaction : reactions) {
            if (reaction.getAnchor().equals(anchor)) {
                return reaction;
            }
        }
        throw new IllegalArgumentException("Experiment doesn't contain reaction with id: " + anchor);
    }

    public ReactionInput locate(ReactionInputMutation mutation) {
        return locateReactionInput(mutation.anchor());
    }

    public ReactionInput locateReactionInput(UUID anchor) {
        for (Reaction reaction : reactions) {
            for (ReactionInput row : reaction.getInputs()) {
                if (row.getAnchor().equals(anchor)) {
                    return row;
                }
            }
        }
        throw new IllegalArgumentException("Reaction doesn't contain input with id: " + anchor);
    }

    public ReactionInputSample locate(ReactionInputSampleMutation mutation) {
        return locateReactionInputSample(mutation.anchor());
    }

    public ReactionInputSample locateReactionInputSample(UUID anchor) {
        for (Reaction reaction : reactions) {
            for (ReactionInput row : reaction.getInputs()) {
                for (ReactionInputSample sample : row.getSamples()) {
                    if (sample.getAnchor().equals(anchor)) {
                        return sample;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Reaction doesn't contain input sample with id: " + anchor);
    }

    public ReactionOutput locate(ReactionOutputMutation mutation) {
        return locateReactionOutput(mutation.anchor());
    }

    public ReactionOutput locateReactionOutput(UUID anchor) {
        for (Reaction reaction : reactions) {
            for (ReactionOutput row : reaction.getOutputs()) {
                if (row.getAnchor().equals(anchor)) {
                    return row;
                }
            }
        }
        throw new IllegalArgumentException("Reaction doesn't contain output with id: " + anchor);
    }

    public ReactionOutputSample locate(ReactionOutputSampleMutation mutation) {
        return locateReactionOutputSample(mutation.anchor());
    }

    public ReactionOutputSample locateReactionOutputSample(UUID anchor) {
        for (Reaction reaction : reactions) {
            for (ReactionOutput row : reaction.getOutputs()) {
                for (ReactionOutputSample sample : row.getSamples()) {
                    if (sample.getAnchor().equals(anchor)) {
                        return sample;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Reaction doesn't contain output sample with id: " + anchor);
    }

    @Override
    public void prepareToRecalculate() {
        for (Reaction reaction : reactions) {
            reaction.prepareToRecalculate();
        }
    }

    @Override
    public void toStringTree(Builder builder) {
        builder.open("Model");
        for (Reaction reaction : reactions) {
            reaction.toStringTree(builder);
        }
        builder.close();
    }

    @Override
    public String toString() {
        return toStringTree();
    }
}
