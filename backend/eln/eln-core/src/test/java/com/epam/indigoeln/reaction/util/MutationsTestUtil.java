package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.client.CompoundClient;
import com.epam.indigoeln.eln.client.ExperimentClient;
import com.epam.indigoeln.eln.model.BaseExperimentDTO;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MutationsTestUtil {

    public static ReactionMutation.ResolveInputs prepareResolveInputs(BaseExperimentDTO experiment, ReactionAnchor reactionAnchor, ExperimentClient experimentClient, CompoundClient compoundClient) {
        ReactionMutation.ResolveInputs mutation = new ReactionMutation.ResolveInputs(reactionAnchor, new HashMap<>(), new HashMap<>());
        Map<InputAnchor, @Nullable FindSamplesRequest> requests = experimentClient.analyzeRXN(experiment.getId(), reactionAnchor);
        requests.forEach((anchor, request) -> {
            if (request != null) {
                Page<SampleDTO> samples = compoundClient.findSamples(request, Paging.DEFAULT);
                if (!samples.getItems().isEmpty()) {
                    mutation.inputSamples().put(anchor, samples.getItems().getFirst().getId());
                    mutation.createdSampleAnchors().put(anchor, new InputSampleAnchor(UUID.randomUUID()));
                }
            }
        });
        return mutation;
    }
}
