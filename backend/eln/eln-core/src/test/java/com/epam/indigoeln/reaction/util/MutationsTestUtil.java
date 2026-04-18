package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.StructuralSearch;
import com.epam.indigoeln.eln.client.CompoundClient;
import com.epam.indigoeln.eln.client.ExperimentClient;
import com.epam.indigoeln.eln.model.BaseExperimentDTO;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.epam.indigoeln.compound.model.search.SearchCatalog.ELN;

public class MutationsTestUtil {

    public static ReactionMutation.ResolveInputs prepareResolveInputs(BaseExperimentDTO experiment, ReactionAnchor reactionAnchor, ExperimentClient experimentClient, CompoundClient compoundClient) {
        ReactionMutation.ResolveInputs mutation = new ReactionMutation.ResolveInputs(reactionAnchor, new HashMap<>());
        Map<InputAnchor, String> requests = experimentClient.analyzeRXN(experiment.getId(), reactionAnchor);
        requests.forEach((anchor, structure) -> {
            FindSamplesRequest findSamplesRequest = new FindSamplesRequest().withCatalogs(Set.of(ELN)).withStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, structure));
            SampleSearchResult samples = compoundClient.search(findSamplesRequest, null, null, Paging.DEFAULT_PAGE_SIZE);
            if (!samples.items().isEmpty()) {
                mutation.inputSamples().put(anchor, samples.items().getFirst().getId());
            }
        });
        return mutation;
    }
}
