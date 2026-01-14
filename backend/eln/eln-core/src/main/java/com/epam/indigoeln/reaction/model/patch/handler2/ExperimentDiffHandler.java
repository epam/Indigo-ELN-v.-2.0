package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.metamodel.ExperimentMetamodel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

public class ExperimentDiffHandler extends MetamodelDiffHandler<ExperimentSnapshot, ExperimentPatch> {

    private final MutationContext context;

    public ExperimentDiffHandler(MutationContext context) {
        super(ExperimentMetamodel.INSTANCE, ExperimentPatch::new);
        this.context = context;
    }

    @Override
    protected void doCompareBase(Flag updated, @Nullable ExperimentSnapshot a, ExperimentSnapshot b, ExperimentPatch patch) {
        super.doCompareBase(updated, a, b, patch);
        updated.set(); // experiment patch is always non-null, even if nothing changed
    }
}
