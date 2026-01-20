package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.metamodel.ExperimentMetamodel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

public class ExperimentDiffHandler extends MetamodelDiffHandler<ExperimentSnapshot, ExperimentPatch> {

    public static final ExperimentDiffHandler INSTANCE = new ExperimentDiffHandler();

    private ExperimentDiffHandler() {
        super(ExperimentMetamodel.INSTANCE, ExperimentPatch::new);
    }

    @Override
    protected void doCompareBase(Flag updated, @Nullable ExperimentSnapshot a, ExperimentSnapshot b, ExperimentPatch patch) {
        super.doCompareBase(updated, a, b, patch);
        updated.set(); // experiment patch is always non-null, even if nothing changed
    }
}
