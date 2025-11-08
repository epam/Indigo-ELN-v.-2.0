package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.patch.ReactionPatch;
import com.epam.indigoeln.reaction.util.Flag;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ReactionValueHandler extends AbstractMetamodelValueHandler<ExperimentModel, Reaction, ReactionPatch> {

    public static final ReactionValueHandler INSTANCE = new ReactionValueHandler();
    public static final ListValueHandler<ExperimentModel, Reaction, Anchor.Reaction, ReactionPatch> LIST_INSTANCE = new ListValueHandler<>(Reaction::getAnchor, INSTANCE);

    private ReactionValueHandler() {
        super(Reaction.METAMODEL);
    }

    @Override
    protected ReactionPatch doCompare(Flag updated, @Nullable Reaction a, Reaction b, @Nullable Optional<Integer> from) {
        ReactionPatch patch = new ReactionPatch();
        doSetFrom(updated, from, patch);
        doCompareBase(updated, a, b, from, patch);
        return patch;
    }

    @Override
    protected Reaction doApply(ExperimentModel container, @Nullable Reaction value, ReactionPatch patch) {
        if (value == null) {
            Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
            value = Reaction.createWithAnchor(container, patch.getAnchor().get());
        }
        doApplyBase(value, patch);
        return value;
    }
}
