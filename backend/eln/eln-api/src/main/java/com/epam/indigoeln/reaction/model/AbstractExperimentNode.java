package com.epam.indigoeln.reaction.model;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public sealed abstract class AbstractExperimentNode<P extends ExperimentNode> implements ExperimentNode permits Reaction, ReactionRow, ReactionSample {

    public void move(P newParent) {
        delete();
        insert(newParent);
    }

    public void insert(P newParent) {
        internalSetParent(newParent);
        //noinspection unchecked
        ((List<AbstractExperimentNode<P>>) internalGetSiblings(newParent)).add(this);
    }

    public void delete() {
        checkState(internalGetSiblings(internalGetParent()).remove(this));
        //noinspection DataFlowIssue
        internalSetParent(null);
    }

    protected abstract P internalGetParent();
    @SuppressWarnings("NullableProblems")
    protected abstract void internalSetParent(P parent);

    protected abstract List<? extends AbstractExperimentNode<P>> internalGetSiblings(P parent);
}
