package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.BaseRevisionEntity;
import com.epam.indigoeln.eln.entity.WithRevision;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public abstract class MutationHandler<T extends Mutation, E extends WithRevision, S, R extends BaseRevisionEntity, C, L extends MutationListener<E, C>> {

    @PersistenceContext
    EntityManager em;

    protected abstract C createContext();

    protected abstract List<L> getListeners();

    public MutationResult<S, C> applyMutation(E entity, T mutation) {
        C context = createContext();
        // do the very early preparation; currently only used by undo/redo handlers
        doPrepare(entity, mutation, context);
        // validate user is allowed to perform this mutation;
        doValidateAccess(entity);
        // validate entity is in appropriate status
        doValidateStatus(entity);
        // make snapshot of "before" state
        S snapshotBefore = doSnapshotBefore(entity, context);
        // calculate next revision number
        Integer revisionNo = doGetRevisionNo(entity, context);
        // augment mutation if needed; for example, pre-assign anchors for created objects to make redo deterministic
        mutation = doPrepareMutation(entity, mutation, context);
        // perform the actual mutation;
        // undo/redo handlers must delegate to undo service
        for (L listener : getListeners()) {
            listener.beforeHandle(entity, context);
        }
        String summary = doHandle(entity, mutation, context, snapshotBefore);
        // flush database to make sure all constraints hold
        em.flush();
        // make snapshot of "after" state
        S snapshotAfter = doSnapshotAfter(entity, context);
        // write changes back to the entity
        JsonNode patch = doUpdateEntity(entity, snapshotBefore, snapshotAfter, context);
        for (L listener : getListeners()) {
            listener.afterUpdateEntity(entity);
        }
        // create revision
        R revision = doCreateRevision(entity, mutation, summary, revisionNo, patch, context, snapshotAfter);
        em.persist(revision);

        return new MutationResult<>(snapshotBefore, snapshotAfter, patch, context);
    }

    protected abstract void doValidateAccess(E entity);

    protected abstract void doValidateStatus(E entity);

    public void doPrepare(E entity, T mutation, C context) {
    }

    protected abstract S doSnapshotBefore(E entity, C context);

    protected T doPrepareMutation(E entity, T mutation, C context) {
        return mutation;
    }

    public abstract String doHandle(E entity, T mutation, C context, S snapshotBefore);

    protected abstract JsonNode doUpdateEntity(E entity, S snapshotBefore, S snapshotAfter, C context);

    protected abstract S doSnapshotAfter(E entity, C context);

    protected abstract R doCreateRevision(E experiment, T mutation, String summary, Integer revisionNo, JsonNode patch, C context, S snapshotAfter);

    public boolean isUndoable() {
        return false;
    }

    public void doRestoreStateAfterUndo(E experiment, S snapshot, T mutation) {
    }

    private Integer doGetRevisionNo(E entity, C context) {
        //noinspection ConstantValue
        Integer revisionNo = entity.getRevision() != null ? entity.getRevision() + 1 : 1;
        entity.setRevision(revisionNo);
        return revisionNo;
    }
}
