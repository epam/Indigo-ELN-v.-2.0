package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.util.Flag;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ExperimentModelValueHandler extends AbstractMetamodelValueHandler<Void, ExperimentModel, ExperimentModelPatch> {

    public static final ExperimentModelValueHandler INSTANCE = new ExperimentModelValueHandler();

    private ExperimentModelValueHandler() {
        super(ExperimentModel.METAMODEL);
    }

    @Override
    protected ExperimentModelPatch doCompare(Flag updated, @Nullable ExperimentModel a, ExperimentModel b, @Nullable Optional<Integer> from) {
        Preconditions.checkState(a != null);
        ExperimentModelPatch patch = new ExperimentModelPatch();
        doCompareBase(updated, a, b, from, patch);
        updated.set();
        return patch;
    }

    @Override
    protected ExperimentModel doApply(Void container, @Nullable ExperimentModel value, ExperimentModelPatch patch) {
        Preconditions.checkState(value != null);
        doApplyBase(value, patch);
        return value;
    }
}
