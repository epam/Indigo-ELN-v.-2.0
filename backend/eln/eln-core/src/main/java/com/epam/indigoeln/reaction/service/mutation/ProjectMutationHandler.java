package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.patch.ProjectPatch;

public interface ProjectMutationHandler<T extends Mutation> extends MutationHandler<T, Void, ProjectEntity, ProjectSnapshot, ProjectPatch> {
}
