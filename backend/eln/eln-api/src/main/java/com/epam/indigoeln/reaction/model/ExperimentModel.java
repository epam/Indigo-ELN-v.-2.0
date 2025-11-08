package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.handler.ReactionValueHandler;
import com.epam.indigoeln.reaction.util.ToStringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"lastUsedAnchor", "lastUsedAnchorCached"})
public final class ExperimentModel implements ExperimentModelNode {

    public static final Metamodel<ExperimentModel, ExperimentModelPatch> METAMODEL = new Metamodel<ExperimentModel, ExperimentModelPatch>("ExperimentModel")
            .listProperty("reactions", ExperimentModel::getReactions, ExperimentModel::setReactions, ExperimentModelPatch::getReactions, ExperimentModelPatch::setReactions, Reaction.METAMODEL, ReactionValueHandler.LIST_INSTANCE)
            ;

    @Valid
    @NotEmpty
    @JsonManagedReference
    private List<Reaction> reactions = List.of();

    @Nullable
    @Deprecated // TODO for compatibility with old JSON, remove when dropping DB
    private Integer lastUsedAnchor;

    @Nullable
    @JsonIgnore
    private Integer lastUsedAnchorCached;

    public int generateNextAnchor() {
        if (lastUsedAnchorCached == null) {
            int last = -1;
            for (Reaction reaction : reactions) {
                last = Math.max(last, reaction.getAnchor().getNumber());
                for (ReactionInput input : reaction.getInputs()) {
                    last = Math.max(last, input.getAnchor().getNumber());
                    for (ReactionInputSample sample : input.getSamples()) {
                        last = Math.max(last, sample.getAnchor().getNumber());
                    }
                }
                for (ReactionOutput output : reaction.getOutputs()) {
                    last = Math.max(last, output.getAnchor().getNumber());
                    for (ReactionOutputSample sample : output.getSamples()) {
                        last = Math.max(last, sample.getAnchor().getNumber());
                    }
                }
            }
            lastUsedAnchorCached = last;
        }
        return ++lastUsedAnchorCached;
    }

    public int generateNextNbkBatchNumber() {
        int lastUsedNumber = reactions.stream()
                .flatMap(r -> r.getOutputs().stream())
                .flatMap(or -> or.getSamples().stream())
                .mapToInt(s -> s.getNbkBatchNumber().getOrdinal())
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
    public String toString() {
        return ToStringUtil.toStringBuild(METAMODEL, this);
    }
}
