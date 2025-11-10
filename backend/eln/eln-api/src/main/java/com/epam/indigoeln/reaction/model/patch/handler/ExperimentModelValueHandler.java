package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

public class ExperimentModelValueHandler extends AbstractMetamodelValueHandler<Void, ExperimentModel, ExperimentModelPatch> {

    public ExperimentModelValueHandler(Metamodel<ExperimentModel, ExperimentModelPatch> metamodel) {
        super(metamodel, ExperimentModelPatch::new);
    }

    @Override
    protected void doCompareBase(Flag updated, @Nullable ExperimentModel a, ExperimentModel b, ExperimentModelPatch patch) {
        super.doCompareBase(updated, a, b, patch);
        updated.set();
    }

    @Override
    protected ExperimentModel createNewValue(Void container, ExperimentModelPatch patch) {
        throw new UnsupportedOperationException();
    }
}
