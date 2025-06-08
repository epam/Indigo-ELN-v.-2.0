package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExperimentModel implements ExperimentModelNode, ToStringTree {

    @Valid
    @NotEmpty
    @JsonManagedReference
    private List<Reaction> reactions;

    public ReactionInput locate(ReactionInputMutation mutation) {
        return reactions.get(mutation.reactionNo()).getInputs().get(mutation.rowNo());
    }

    public ReactionInputSample locate(ReactionInputSampleMutation mutation) {
        return reactions.get(mutation.reactionNo()).getInputs().get(mutation.rowNo()).getSamples().get(mutation.sampleNo());
    }

    public ReactionOutput locate(ReactionOutputMutation mutation) {
        return reactions.get(mutation.reactionNo()).getOutputs().get(mutation.rowNo());
    }

    public ReactionOutputSample locate(ReactionOutputSampleMutation mutation) {
        return reactions.get(mutation.reactionNo()).getOutputs().get(mutation.rowNo()).getSamples().get(mutation.sampleNo());
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
