package com.epam.indigoeln.reaction.service.mutation.project;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.ProjectRevisionEntity;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.service.mutation.AbstractMutationContext;
import com.epam.indigoeln.reaction.service.mutation.ProjectMutationListener;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectMutationContext extends AbstractMutationContext<ProjectEntity, ProjectSnapshot, ProjectRevisionEntity, ProjectMutationContext, ProjectMutationListener> {
}
