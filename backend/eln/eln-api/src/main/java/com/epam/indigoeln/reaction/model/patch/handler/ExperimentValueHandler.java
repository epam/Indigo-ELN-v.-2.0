package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.metamodel.ModelProperty;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

public class ExperimentValueHandler extends AbstractMetamodelValueHandler<Void, ExperimentSnapshot, ExperimentPatch> {

    private final MutationContext context;

    public ExperimentValueHandler(MutationContext context) {
        super(Handlers.EXPERIMENT_METAMODEL, ExperimentPatch::new);
        this.context = context;
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

    @Override
    protected <T, P> void doCompareProperty(Flag updated, @Nullable T a, T b, P patch, ModelProperty<T, Object, P, Object> simpleProperty) {
        if (isPropertyAllowed(simpleProperty)) {
            super.doCompareProperty(updated, a, b, patch, simpleProperty);
        }
    }

    @Override
    protected <T, P> void doApplyProperty(T value, P patch, ModelProperty<T, Object, P, Object> simpleProperty) {
        if (isPropertyAllowed(simpleProperty)) {
            super.doApplyProperty(value, patch, simpleProperty);
        }
    }

    private <T, P> boolean isPropertyAllowed(ModelProperty<T, Object, P, Object> simpleProperty) {
        return switch (simpleProperty.name()) {
            case "attachments" -> context.isAffectsAttachments();
            case "acl" -> context.isAffectsACL();
            case "model" -> context.isAffectsModel();
            default -> true;
        };
    }
}
