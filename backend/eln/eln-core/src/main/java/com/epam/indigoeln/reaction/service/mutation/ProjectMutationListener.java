package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.reaction.service.mutation.project.ProjectMutationContext;

public interface ProjectMutationListener extends MutationListener<ProjectEntity, ProjectMutationContext> {
    
}
