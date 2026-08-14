package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentReferencedCompound;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.Set;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateCompoundReferencesListener implements ExperimentMutationListener {

    @Inject
    ExperimentModelHelperService experimentModelHelperService;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Set<ExperimentReferencedCompound> oldCompoundRefs;

    @Override
    public void beforeHandle(ExperimentEntity experiment, ExperimentMutationContext context) {
        oldCompoundRefs = collectCompoundRefs(experiment.getModel());
    }

    @Override
    public void afterRecalculate(ExperimentEntity experiment, ExperimentMutationContext context) {
        Set<ExperimentReferencedCompound> newCompoundRefs = collectCompoundRefs(experiment.getModel());
        if (!oldCompoundRefs.equals(newCompoundRefs)) {
            ModelUtil.updateCollection(experiment.getReferencedCompounds(), newCompoundRefs);
            context.setCompoundsChanged(true);
        }
    }

    private static Set<ExperimentReferencedCompound> collectCompoundRefs(ExperimentModel model) {
        Set<ExperimentReferencedCompound> refs = new HashSet<>();
        for (Reaction reaction : model.getReactions()) {
            for (ReactionInput input : reaction.getInputs()) {
                if (input.getCompound() instanceof CompoundRef.StoredOrVirtual c) {
                    refs.add(new ExperimentReferencedCompound(input.getRole(), c.getCompoundID()));
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                if (output.getCompound() instanceof CompoundRef.StoredOrVirtual c) {
                    refs.add(new ExperimentReferencedCompound(ReactionRole.OUTPUT, c.getCompoundID()));
                }
            }
        }
        return refs;
    }
}
