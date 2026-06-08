package com.epam.indigoeln.reaction.service.mutation.notebook;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.NotebookRevisionEntity;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.service.mutation.AbstractMutationContext;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NotebookMutationContext extends AbstractMutationContext<NotebookEntity, NotebookSnapshot, NotebookRevisionEntity, NotebookMutationContext> {
}
