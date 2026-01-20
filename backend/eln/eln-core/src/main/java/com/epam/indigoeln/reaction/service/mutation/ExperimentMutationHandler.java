package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;

public interface ExperimentMutationHandler<T extends Mutation, R extends MutationRedoInfo> extends MutationHandler<T, ExperimentModel, R, ExperimentEntity, ExperimentSnapshot, ExperimentPatch> {

    Pair<ExperimentSnapshot, ExperimentPatch> applyMutation(ExperimentEntity experiment, T mutation);
}
