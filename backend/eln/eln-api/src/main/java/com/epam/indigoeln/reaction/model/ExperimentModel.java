package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ExperimentModel implements ExperimentNode {

    public static final int DEFAULT_SIGNIFICANT_FIGURES = 5;

    @NotEmpty
    @JsonManagedReference
    private List<@Valid Reaction> reactions = List.of();

    @NotNull
    private int significantFigures = DEFAULT_SIGNIFICANT_FIGURES;

    @NotNull
    private UnitSource unitSource;

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
        Reaction reaction = tryLocate(anchor);
        checkArgument(reaction != null, "Experiment doesn't contain reaction with id: %s", anchor);
        return reaction;
    }

    @Nullable
    public Reaction tryLocate(ReactionAnchor anchor) {
        for (Reaction reaction : reactions) {
            if (reaction.getAnchor().equals(anchor)) {
                return reaction;
            }
        }
        return null;
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

    public static class Deserializer extends DelegatingDeserializer {

        public Deserializer(JsonDeserializer<?> delegate) {
            super(delegate);
        }

        @Override
        protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
            return new Deserializer(newDelegatee);
        }

        @Override
        public ExperimentModel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode tree = ctxt.readTree(p);
            UnitSource unitSource = ctxt.readTreeAsValue(tree.get("unitSource"), UnitSource.class);
            ctxt.setAttribute(UnitSource.CONTEXT_KEY, unitSource);
            try (JsonParser treeParser = tree.traverse(p.getCodec())) {
                treeParser.nextToken();
                return (ExperimentModel) _delegatee.deserialize(treeParser, ctxt);
            }
        }
    }
}
