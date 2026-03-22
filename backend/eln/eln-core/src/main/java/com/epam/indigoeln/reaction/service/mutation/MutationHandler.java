package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.BaseRevisionEntity;
import com.epam.indigoeln.eln.entity.WithRevision;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public abstract class MutationHandler<T extends Mutation, E extends WithRevision, S, R extends BaseRevisionEntity, C> {

    @PersistenceContext
    EntityManager em;

    protected abstract C createContext();

    public Pair<S, JsonNode> applyMutation(E entity, T mutation) {
        C context = createContext();
        // do the very early preparation; currently only used by undo/redo handlers
        doPrepare(entity, mutation, context);
        // validate user is allowed to perform this mutation;
        doValidateAccess(entity, mutation, context);
        // make snapshot of "before" state
        S snapshotBefore = doSnapshotBefore(entity, context);
        // calculate next revision number
        Integer revisionNo = doGetRevisionNo(entity, context);
        // augment mutation if needed; for example, pre-assign anchors for created objects to make redo deterministic
        mutation = doPrepareMutation(entity, mutation, context);
        // perform the actual mutation;
        // undo/redo handlers must delegate to undo service
        MutationResult result = doHandle(entity, mutation, context, snapshotBefore);
        // flush database to make sure all constraints hold
        em.flush();
        // make snapshot of "after" state
        S snapshotAfter = doSnapshotAfter(entity, context);
        // write changes back to the entity
        JsonNode patch = doUpdateEntity(entity, snapshotBefore, snapshotAfter, context);
        // create revision
        R revision = doCreateRevision(entity, mutation, result, revisionNo, patch, context, snapshotAfter);
        em.persist(revision);

        return Pair.of(snapshotAfter, patch);
    }

    protected abstract void doValidateAccess(E entity, T mutation, C context);

    public void doPrepare(E entity, T mutation, C context) {
    }

    protected abstract S doSnapshotBefore(E entity, C context);

    protected T doPrepareMutation(E entity, T mutation, C context) {
        return mutation;
    }

    public abstract MutationResult doHandle(E entity, T mutation, C context, S snapshotBefore);

    protected abstract JsonNode doUpdateEntity(E entity, S snapshotBefore, S snapshotAfter, C context);

    protected abstract S doSnapshotAfter(E entity, C context);

    protected abstract R doCreateRevision(E experiment, T mutation, MutationResult result, Integer revisionNo, JsonNode patch, C context, S snapshotAfter);

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
