package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.patch.ReactionInputPatch;
import com.epam.indigoeln.reaction.util.Flag;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ReactionInputValueHandler extends AbstractMetamodelValueHandler<Reaction, ReactionInput, ReactionInputPatch> {

    public static final ReactionInputValueHandler INSTANCE = new ReactionInputValueHandler();
    public static final ListValueHandler<Reaction, ReactionInput, Anchor.Input, ReactionInputPatch> LIST_INSTANCE = new ListValueHandler<>(ReactionInput::getAnchor, INSTANCE);

    private ReactionInputValueHandler() {
        super(ReactionInput.METAMODEL);
    }

    @Override
    protected ReactionInputPatch doCompare(Flag updated, @Nullable ReactionInput a, ReactionInput b, @Nullable Optional<Integer> from) {
        ReactionInputPatch patch = new ReactionInputPatch();
        doSetFrom(updated, from, patch);
        doCompareBase(updated, a, b, from, patch);
        return patch;
    }

    @Override
    protected ReactionInput doApply(Reaction container, @Nullable ReactionInput value, ReactionInputPatch patch) {
        if (value == null) {
            Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
            value = ReactionInput.createWithAnchor(container, patch.getAnchor().get());
        }
        doApplyBase(value, patch);
        return value;
    }
}
