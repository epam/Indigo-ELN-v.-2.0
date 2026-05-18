package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReactionMutation extends Mutation {

    ReactionAnchor anchor();

    record SetScheme (
        @NotNull ReactionAnchor anchor,
        @Nullable String rxnFile,
        @Nullable List<InputAnchor> createdReactantAnchors,
        @Nullable List<InputSampleAnchor> createdReactantSampleAnchors,
        @Nullable List<InputAnchor> createdCatalystAnchors,
        @Nullable List<InputSampleAnchor> createdCatalystSampleAnchors,
        @Nullable List<OutputAnchor> createdProductAnchors
    ) implements ReactionMutation {
        public SetScheme(ReactionAnchor anchor, String rxnFile) {
            this(anchor, rxnFile, null, null, null, null, null);
        }

        @Override
        public String toString() {
            return "SetScheme[" +
                    "anchor=" + anchor +
                    ']';
        }
    }

    record ResolveInputs (
            @NotNull ReactionAnchor anchor,
            @NotEmpty Map<InputAnchor, UUID> inputSamples, // anchor -> sampleID
            @Nullable Map<InputAnchor, InputSampleAnchor> createdSampleAnchors
    ) implements ReactionMutation {
        public ResolveInputs(ReactionAnchor anchor, Map<InputAnchor, UUID> inputSamples) {
            this(anchor, inputSamples, null);
        }
    }

    record AddEmptyInput (
        @NotNull ReactionAnchor anchor,
        @Nullable InputAnchor createdInputAnchor,
        @Nullable InputSampleAnchor createdSampleAnchor
    ) implements ReactionMutation {
        public AddEmptyInput(ReactionAnchor anchor) {
            this(anchor, null, null);
        }
    }

    record AddInput (
        @NotNull ReactionAnchor anchor,
        @NotNull UUID sampleId,
        @Nullable InputAnchor createdInputAnchor,
        @Nullable InputSampleAnchor createdSampleAnchor
    ) implements ReactionMutation {
        public AddInput(ReactionAnchor anchor, UUID sampleId) {
            this(anchor, sampleId, null, null);
        }
    }

    record ImportSDF (
        @NotNull ReactionAnchor anchor,
        @NotNull List<@NotNull UUID> compoundIDs,
        @Nullable List<OutputAnchor> createdOutputAnchors,
        @Nullable List<OutputSampleAnchor> createdSampleAnchors
    ) implements ReactionMutation {
        public ImportSDF(ReactionAnchor anchor, List<UUID> compoundIDs) {
            this(anchor, compoundIDs, null, null);
        }
    }
}
