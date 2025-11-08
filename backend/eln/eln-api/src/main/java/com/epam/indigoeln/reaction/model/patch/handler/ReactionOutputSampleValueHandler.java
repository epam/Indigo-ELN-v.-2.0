package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputSamplePatch;
import com.epam.indigoeln.reaction.util.Flag;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ReactionOutputSampleValueHandler extends AbstractMetamodelValueHandler<ReactionOutput, ReactionOutputSample, ReactionOutputSamplePatch> {

    public static final ReactionOutputSampleValueHandler INSTANCE = new ReactionOutputSampleValueHandler();
    public static final ListValueHandler<ReactionOutput, ReactionOutputSample, Anchor.OutputSample, ReactionOutputSamplePatch> LIST_INSTANCE = new ListValueHandler<>(ReactionOutputSample::getAnchor, INSTANCE);

    private ReactionOutputSampleValueHandler() {
        super(ReactionOutputSample.METAMODEL);
    }

    @Override
    protected ReactionOutputSamplePatch doCompare(Flag updated, @Nullable ReactionOutputSample a, ReactionOutputSample b, @Nullable Optional<Integer> from) {
        ReactionOutputSamplePatch patch = new ReactionOutputSamplePatch();
        doSetFrom(updated, from, patch);
        doCompareBase(updated, a, b, from, patch);
        return patch;
    }

    @Override
    protected ReactionOutputSample doApply(ReactionOutput container, @Nullable ReactionOutputSample value, ReactionOutputSamplePatch patch) {
        if (value == null) {
            Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
            value = ReactionOutputSample.createWithAnchor(container, patch.getAnchor().get());
        }
        doApplyBase(value, patch);
        return value;
    }
}
