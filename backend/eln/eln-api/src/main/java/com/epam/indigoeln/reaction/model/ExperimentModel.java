package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.mutation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ExperimentModel implements ExperimentNode {

    public static final int SCHEMA_VERSION = 1;

    @NotEmpty
    @JsonManagedReference
    private List<@Valid Reaction> reactions = List.of();

    @NotNull
    private Integer schemaVersion;

    public int generateNextNbkBatchNumber() {
        int[] last = {0};
        for (Reaction reaction : reactions) {
            for (ReactionOutput output : reaction.getOutputs()) {
                for (ReactionOutputSample sample : output.getSamples()) {
                    last[0] = Math.max(last[0], sample.getNbkBatchNumber().getOrdinal());
                }
            }
        }
        return last[0] + 1;
    }

    public Reaction locate(ReactionMutation mutation) {
        return locate(mutation.anchor());
    }

    public Reaction locate(ReactionAnchor anchor) {
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

    public ReactionInput locate(InputAnchor anchor) {
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

    public ReactionInputSample locate(InputSampleAnchor anchor) {
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

    public ReactionOutput locate(OutputAnchor anchor) {
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

    public ReactionOutputSample locate(OutputSampleAnchor anchor) {
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
}
