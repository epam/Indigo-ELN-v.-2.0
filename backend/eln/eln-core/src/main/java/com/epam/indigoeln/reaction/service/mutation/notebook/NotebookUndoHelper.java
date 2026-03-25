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
        // !!! load last 1 hour, and then drop leftmost revisions, so all remaining undo/redo has their initial revision loaded
        return notebookRepository.findRecentRevisions(entity, Duration.of(365, ChronoUnit.DAYS));
    }
}
