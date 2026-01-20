package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.metamodel.ProjectMetamodel;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.patch.ProjectPatch;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

public class ProjectDiffHandler extends MetamodelDiffHandler<ProjectSnapshot, ProjectPatch> {

    public static final ProjectDiffHandler INSTANCE = new ProjectDiffHandler();

    private ProjectDiffHandler() {
        super(ProjectMetamodel.INSTANCE, ProjectPatch::new);
    }

    @Override
    protected void doCompareBase(Flag updated, @Nullable ProjectSnapshot a, ProjectSnapshot b, ProjectPatch patch) {
        super.doCompareBase(updated, a, b, patch);
        updated.set(); // project patch is always non-null, even if nothing changed
    }
}
