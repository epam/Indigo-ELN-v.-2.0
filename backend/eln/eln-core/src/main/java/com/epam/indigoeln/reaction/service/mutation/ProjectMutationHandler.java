package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.patch.ProjectPatch;

public interface ProjectMutationHandler<T extends Mutation, R extends MutationRedoInfo> extends MutationHandler<T, Void, R, ProjectEntity, ProjectSnapshot, ProjectPatch> {
}
