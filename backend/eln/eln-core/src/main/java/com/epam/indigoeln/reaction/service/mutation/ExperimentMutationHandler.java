package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import org.jspecify.annotations.Nullable;

public interface ExperimentMutationHandler<T extends Mutation, R extends MutationRedoInfo> extends MutationHandler {

    // !!! remove from public interface
    MutationResult handle(ExperimentEntity experiment, @Nullable ExperimentModel model, T mutation, @Nullable R redoInfo, MutationContext context);

    Pair<ExperimentSnapshot, ExperimentPatch> applyMutation(ExperimentEntity experiment, T mutation);
}
