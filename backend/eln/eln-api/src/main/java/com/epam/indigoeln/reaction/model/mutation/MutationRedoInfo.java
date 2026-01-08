package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(MutationRedoInfo.AddInput.class),
        @JsonSubTypes.Type(MutationRedoInfo.SetScheme.class),
        @JsonSubTypes.Type(MutationRedoInfo.ResolveInputs.class),
        @JsonSubTypes.Type(MutationRedoInfo.AddOutputSample.class),
})
public interface MutationRedoInfo {

    record AddInput (
            Pair<Anchor.Input, Anchor.InputSample> anchors
    ) implements MutationRedoInfo {
    }

    record SetScheme (
            List<Pair<Anchor.Input, Anchor.InputSample>> reactantAnchors,
            List<Pair<Anchor.Input, Anchor.InputSample>> catalystAnchors,
            List<Anchor.Output> productAnchors
    ) implements MutationRedoInfo {
    }

    record ResolveInputs (
            Map<Anchor.Input, Anchor.InputSample> sampleAnchors
    ) implements MutationRedoInfo {
    }

    record AddOutputSample (
            Anchor.OutputSample anchor
    ) implements MutationRedoInfo {
    }
}
