package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.mutation.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public final class ExperimentModel implements ExperimentModelNode, ToStringTree {

    @Valid
    @NotEmpty
    @JsonManagedReference
    private List<Reaction> reactions;

    private int lastUsedAnchor = 0;

    public int generateNextAnchor() {
        return ++lastUsedAnchor;
    }

    public int generateNextNotebookBatchNumber() {
        int lastUsedNumber = reactions.stream()
                .flatMap(r -> r.getOutputs().stream())
                .flatMap(or -> or.getSamples().stream())
                .mapToInt(s -> s.getNotebookBatchNumber().getOrdinal())
                .max().orElse(0);
        return lastUsedNumber + 1;
    }

    public Reaction locate(ReactionMutation mutation) {
        return locate(mutation.anchor());
    }

    public Reaction locate(Anchor.Reaction anchor) {
        for (Reaction reaction : reactions) {
            if (reaction.getAnchor().equals(anchor)) {
                return reaction;
            }
        }
        throw new IllegalArgumentException("Experiment doesn't contain reaction with id: " + anchor);
    }

    public ReactionInput locate(ReactionInputMutation mutation) {
        return locate(mutation.anchor());
    }

    public ReactionInput locate(Anchor.Input anchor) {
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
        return locate(mutation.anchor());
    }

    public ReactionInputSample locate(Anchor.InputSample anchor) {
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
        return locate(mutation.anchor());
    }

    public ReactionOutput locate(Anchor.Output anchor) {
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
        return locate(mutation.anchor());
    }

    public ReactionOutputSample locate(Anchor.OutputSample anchor) {
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
