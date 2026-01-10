package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.reaction.model.*;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
            Pair<InputAnchor, InputSampleAnchor> anchors
    ) implements MutationRedoInfo {
    }

    record SetScheme (
            List<Pair<InputAnchor, InputSampleAnchor>> reactantAnchors,
            List<Pair<InputAnchor, InputSampleAnchor>> catalystAnchors,
            List<OutputAnchor> productAnchors
    ) implements MutationRedoInfo {
    }

    record ResolveInputs (
            Map<InputAnchor, InputSampleAnchor> sampleAnchors
    ) implements MutationRedoInfo {
    }

    record AddOutputSample (
            OutputSampleAnchor anchor
    ) implements MutationRedoInfo {
    }
}
