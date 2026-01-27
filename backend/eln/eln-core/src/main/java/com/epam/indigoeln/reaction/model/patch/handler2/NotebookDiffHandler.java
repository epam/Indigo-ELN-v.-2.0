package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.metamodel.NotebookMetamodel;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

public class NotebookDiffHandler extends MetamodelDiffHandler<NotebookSnapshot, NotebookPatch> {

    public static final NotebookDiffHandler INSTANCE = new NotebookDiffHandler();

    private NotebookDiffHandler() {
        super(NotebookMetamodel.INSTANCE, NotebookPatch::new);
    }

    @Override
    protected void doCompareBase(Flag updated, @Nullable NotebookSnapshot a, NotebookSnapshot b, NotebookPatch patch) {
        super.doCompareBase(updated, a, b, patch);
        updated.set(); // notebook patch is always non-null, even if nothing changed
    }
}
