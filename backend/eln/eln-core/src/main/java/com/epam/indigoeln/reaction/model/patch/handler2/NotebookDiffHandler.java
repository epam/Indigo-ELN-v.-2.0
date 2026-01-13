package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.metamodel.NotebookMetamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutationContext;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

public class NotebookDiffHandler extends MetamodelDiffHandler<NotebookSnapshot, NotebookPatch> {

    private final NotebookMutationContext context;

    public NotebookDiffHandler(NotebookMutationContext context) {
        super(NotebookMetamodel.INSTANCE, NotebookPatch::new);
        this.context = context;
    }

    @Override
    protected void doCompareBase(Flag updated, @Nullable NotebookSnapshot a, NotebookSnapshot b, NotebookPatch patch) {
        super.doCompareBase(updated, a, b, patch);
        updated.set(); // notebook patch is always non-null, even if nothing changed
    }

    @Override
    protected void doCompareProperty(Flag updated, @Nullable NotebookSnapshot a, NotebookSnapshot b, NotebookPatch patch, ModelProperty<NotebookSnapshot, Object, NotebookPatch, Object> simpleProperty) {
        if (isPropertyAllowed(simpleProperty)) {
            super.doCompareProperty(updated, a, b, patch, simpleProperty);
        }
    }

    private <T, P> boolean isPropertyAllowed(ModelProperty<T, Object, P, Object> simpleProperty) {
        return switch (simpleProperty.name()) {
            case "attachments" -> context.isAffectsAttachments();
            case "acl" -> context.isAffectsACL();
            default -> true;
        };
    }
}
