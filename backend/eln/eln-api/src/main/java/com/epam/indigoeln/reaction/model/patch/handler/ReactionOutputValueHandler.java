package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputPatch;
import com.epam.indigoeln.reaction.util.Flag;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ReactionOutputValueHandler extends AbstractMetamodelValueHandler<Reaction, ReactionOutput, ReactionOutputPatch> {

    public static final ReactionOutputValueHandler INSTANCE = new ReactionOutputValueHandler();
    public static final ListValueHandler<Reaction, ReactionOutput, Anchor.Output, ReactionOutputPatch> LIST_INSTANCE = new ListValueHandler<>(ReactionOutput::getAnchor, INSTANCE);

    private ReactionOutputValueHandler() {
        super(ReactionOutput.METAMODEL);
    }

    @Override
    protected ReactionOutputPatch doCompare(Flag updated, @Nullable ReactionOutput a, ReactionOutput b, @Nullable Optional<Integer> from) {
        ReactionOutputPatch patch = new ReactionOutputPatch();
        doSetFrom(updated, from, patch);
        doCompareBase(updated, a, b, from, patch);
        return patch;
    }

    @Override
    protected ReactionOutput doApply(Reaction container, @Nullable ReactionOutput value, ReactionOutputPatch patch) {
        if (value == null) {
            Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
            value = ReactionOutput.createWithAnchor(container, patch.getAnchor().get());
        }
        doApplyBase(value, patch);
        return value;
    }
}
