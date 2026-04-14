package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.service.AbstractUndoHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class ExperimentUndoHelper extends AbstractUndoHelper<ExperimentEntity, ExperimentSnapshot, ExperimentRevisionEntity, ExperimentMutationContext> {

    @Inject
    ExperimentRepository experimentRepository;

    ExperimentUndoHelper() {
        super(ExperimentSnapshot.class);
    }

    @Override
    protected List<ExperimentRevisionEntity> loadRevisions(ExperimentEntity entity) {
        // !!! load last 1 hour, and then drop leftmost revisions, so all remaining undo/redo has their initial revision loaded
        return experimentRepository.findRecentRevisions(entity, Duration.of(365, ChronoUnit.DAYS));
    }

    @Override
    protected void restoreStateAfterUndo(ExperimentEntity entity, ExperimentMutationContext context, ExperimentSnapshot snapshot, UndoInfo info) {
        if (context.isAffectsModel()) {
            entity.setModelObj(snapshot.getModel());
        }
        super.restoreStateAfterUndo(entity, context, snapshot, info);
    }

    @Override
    protected void afterHandle(AbstractUndoHelper<ExperimentEntity, ExperimentSnapshot, ExperimentRevisionEntity, ExperimentMutationContext>.UndoInfo info, ExperimentMutationContext context, boolean redo) {
        context.getResponse().getMessages().add((redo ? "Redone: " : "Undone: ") + info.getRevision().getRevisionSummary());
    }
}
