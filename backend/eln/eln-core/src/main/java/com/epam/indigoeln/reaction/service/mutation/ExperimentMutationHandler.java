package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.JsonNode;

public interface ExperimentMutationHandler<T extends Mutation> extends MutationHandler<T, ExperimentModel, ExperimentEntity, ExperimentSnapshot> {

    Pair<ExperimentSnapshot, JsonNode> applyMutation(ExperimentEntity experiment, T mutation);
}
