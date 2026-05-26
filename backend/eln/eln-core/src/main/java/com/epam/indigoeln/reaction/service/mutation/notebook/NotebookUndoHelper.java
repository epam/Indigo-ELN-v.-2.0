package com.epam.indigoeln.reaction.service.mutation.notebook;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.NotebookRevisionEntity;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.service.AbstractUndoHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class NotebookUndoHelper extends AbstractUndoHelper<NotebookEntity, NotebookSnapshot, NotebookRevisionEntity, NotebookMutationContext> {

    @Inject
    NotebookRepository notebookRepository;

    NotebookUndoHelper() {
        super(NotebookSnapshot.class);
    }

    @Override
    protected List<NotebookRevisionEntity> loadRevisions(NotebookEntity entity) {
        // TODO load last 1 hour, and either deny undoing beyond that, or load previous data if needed for redo
        return notebookRepository.findRecentRevisions(entity, Duration.of(365, ChronoUnit.DAYS));
    }
}
