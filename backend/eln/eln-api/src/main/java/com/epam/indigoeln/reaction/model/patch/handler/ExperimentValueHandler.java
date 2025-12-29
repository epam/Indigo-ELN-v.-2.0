package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

public class ExperimentValueHandler extends AbstractMetamodelValueHandler<Void, ExperimentSnapshot, ExperimentPatch> {

    public ExperimentValueHandler(Metamodel<ExperimentSnapshot, ExperimentPatch> metamodel) {
        super(metamodel, ExperimentPatch::new);
    }

    @Override
    protected void doCompareBase(Flag updated, @Nullable ExperimentSnapshot a, ExperimentSnapshot b, ExperimentPatch patch) {
        super.doCompareBase(updated, a, b, patch);
        updated.set();
    }

    @Override
    protected ExperimentSnapshot createNewValue(Void container, ExperimentPatch patch) {
        throw new UnsupportedOperationException();
    }
}
