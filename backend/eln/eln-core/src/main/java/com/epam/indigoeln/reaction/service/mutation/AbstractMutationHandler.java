package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.WithRevision;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jspecify.annotations.Nullable;

public abstract class AbstractMutationHandler<T extends Mutation, M, E extends WithRevision, S, P> implements MutationHandler<T, M, E, S, P> {

    @PersistenceContext
    EntityManager em;

    public boolean isAffectsAttachments() {
        return false;
    }

    public boolean isAffectsACL() {
        return false;
    }

    public boolean isAffectsModel() {
        return false;
    }

    @Override
    public Pair<S, P> applyMutation(E entity, T mutation) {
        // do the very early preparation; currently only used by undo/redo handlers
        doPrepare(entity, mutation); // !!! only used by undo/redo, remove?
        // validate user is allowed to do this mutation;
        // undo/redo handlers must delegate to reverse/initial handler
        doValidateAccess(entity, mutation);
        // make snapshot of "before" state
        S snapshotBefore = doSnapshotBefore(entity);
        // calculate next revision number
        Integer revisionNo = doGetRevisionNo(entity);
        // prepare model; only used for subset of experiment handlers that work with experiment model
        M model = doPrepareModel(entity);
        // perform the actual mutation;
        // undo/redo handlers must delegate to reverse/initial handler
        MutationResult result = doHandle(entity, model, mutation);
        // flush database to make sure all constraints hold
        em.flush();
        // make snapshot of "after" state
        S snapshotAfter = doSnapshotAfter(entity, model);
        // write changes back to the entity
        doUpdateEntity(entity, model, snapshotBefore, snapshotAfter);
        // create patch
        P patch = doCreatePatch(snapshotBefore, snapshotAfter);
        // create revision
        doCreateRevision(entity, mutation, result, revisionNo, patch);

        return Pair.of(snapshotAfter, patch);
    }

    protected abstract void doValidateAccess(E entity, T mutation);

    protected void doPrepare(E entity, T mutation) {
    }

    protected abstract S doSnapshotBefore(E entity);

    @Nullable
    protected M doPrepareModel(E entity) {
        return null;
    }

    public abstract MutationResult doHandle(E entity, @Nullable M model, T mutation);

    protected abstract void doUpdateEntity(E entity, @Nullable M model, S snapshotBefore, S snapshotAfter);

    protected abstract S doSnapshotAfter(E entity, @Nullable M model);

    protected abstract P doCreatePatch(S snapshotBefore, S snapshotAfter);

    protected abstract void doCreateRevision(E experiment, T mutation, MutationResult result, Integer revisionNo, P patch);

    private Integer doGetRevisionNo(E entity) {
        //noinspection ConstantValue
        Integer revisionNo = entity.getRevision() != null ? entity.getRevision() + 1 : 1;
        entity.setRevision(revisionNo);
        return revisionNo;
    }
}
