package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReactionMutation extends ExperimentMutation {

    ReactionAnchor anchor();

    @Override
    default boolean isApplicableToEditSession() {
        return true;
    }

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
            @NotEmpty Map<InputAnchor, SampleDTO> inputSamples,
            @Nullable Map<InputAnchor, InputSampleAnchor> createdSampleAnchors
    ) implements ReactionMutation {
        public ResolveInputs(ReactionAnchor anchor, Map<InputAnchor, SampleDTO> inputSamples) {
            this(anchor, inputSamples, null);
        }
    }

    record AddEmptyInput (
        @NotNull ReactionAnchor anchor,
        InputAnchor createdInputAnchor,
        InputSampleAnchor createdSampleAnchor
    ) implements ReactionMutation {
        public AddEmptyInput(ReactionAnchor anchor) {
            this(anchor, null, null);
        }
    }

    record AddInput (
        @NotNull ReactionAnchor anchor,
        @NotNull SampleDTO sample,
        InputAnchor createdInputAnchor,
        InputSampleAnchor createdSampleAnchor
    ) implements ReactionMutation {
        public AddInput(ReactionAnchor anchor, SampleDTO sample) {
            this(anchor, sample, null, null);
        }
    }

    record AddNoProductSample (
            @NotNull ReactionAnchor anchor,
            OutputAnchor createdOutputAnchor,
            OutputSampleAnchor createdSampleAnchor
    ) implements ReactionMutation {
        public AddNoProductSample(ReactionAnchor anchor) {
            this(anchor, null, null);
        }
    }

    record ImportSDF (
        @NotNull ReactionAnchor anchor,
        @NotNull List<UUID> compoundIDs,
        @NotNull List<SampleDTO> samples,
        List<OutputAnchor> createdOutputAnchors,
        List<OutputSampleAnchor> createdSampleAnchors
    ) implements ReactionMutation {
        public ImportSDF(ReactionAnchor anchor, List<UUID> compoundIDs, List<SampleDTO> samples) {
            //noinspection DataFlowIssue
            this(anchor, compoundIDs, samples, null, null);
        }

        @Override
        public boolean isMutateMethodAllowed() {
            return false;
        }
    }
}
