package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.patch.ReactionInputSamplePatch;
import com.epam.indigoeln.reaction.util.Flag;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ReactionInputSampleValueHandler extends AbstractMetamodelValueHandler<ReactionInput, ReactionInputSample, ReactionInputSamplePatch> {

    public static final ReactionInputSampleValueHandler INSTANCE = new ReactionInputSampleValueHandler();
    public static final ListValueHandler<ReactionInput, ReactionInputSample, Anchor.InputSample, ReactionInputSamplePatch> LIST_INSTANCE = new ListValueHandler<>(ReactionInputSample::getAnchor, INSTANCE);

    private ReactionInputSampleValueHandler() {
        super(ReactionInputSample.METAMODEL);
    }

    @Override
    protected ReactionInputSamplePatch doCompare(Flag updated, @Nullable ReactionInputSample a, ReactionInputSample b, @Nullable Optional<Integer> from) {
        ReactionInputSamplePatch patch = new ReactionInputSamplePatch();
        doSetFrom(updated, from, patch);
        doCompareBase(updated, a, b, from, patch);
        return patch;
    }

    @Override
    protected ReactionInputSample doApply(ReactionInput container, @Nullable ReactionInputSample value, ReactionInputSamplePatch patch) {
        if (value == null) {
            Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
            value = ReactionInputSample.createWithAnchor(container, patch.getAnchor().get());
        }
        doApplyBase(value, patch);
        return value;
    }
}
