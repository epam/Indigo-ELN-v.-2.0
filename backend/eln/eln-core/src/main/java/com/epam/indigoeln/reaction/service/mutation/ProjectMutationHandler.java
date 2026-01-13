package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutationContext;
import org.jspecify.annotations.Nullable;

public interface ProjectMutationHandler<T extends Mutation, R extends MutationRedoInfo> extends MutationHandler {

    MutationResult handle(ProjectEntity project, T mutation, @Nullable R redoInfo, ProjectMutationContext context);

    void initContext(ProjectEntity project, T mutation, ProjectMutationContext context);
}
