package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentReferencedCompound;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import com.epam.indigoeln.reaction.service.mutation.ExperimentModelMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.Set;

@Dependent
@Priority(ExperimentModelMutationListener.DEFAULT_PRIORITY)
public class UpdateCompoundReferencesListener implements ExperimentModelMutationListener {

    @Inject
    ExperimentModelHelperService experimentModelHelperService;

    private final Set<ExperimentReferencedCompound> oldCompoundRefs = new HashSet<>();

    @Override
    public void beforeHandle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutationContext context) {
        collectCompoundRefs(model, oldCompoundRefs);
    }

    @Override
    public void afterRecalculate(ExperimentEntity experiment, ExperimentModel model, ExperimentMutationContext context) {
        Set<ExperimentReferencedCompound> newCompoundRefs = new HashSet<>();
        collectCompoundRefs(model, newCompoundRefs);
        if (!oldCompoundRefs.equals(newCompoundRefs)) {
            ModelUtil.updateCollection(experiment.getReferencedCompounds(), newCompoundRefs);
        }
    }

    private static void collectCompoundRefs(ExperimentModel model, Set<ExperimentReferencedCompound> target) {
        for (Reaction reaction : model.getReactions()) {
            for (ReactionInput input : reaction.getInputs()) {
                if (input.getCompound() instanceof CompoundRef.StoredOrVirtual c) {
                    target.add(new ExperimentReferencedCompound(input.getRole(), c.getCompoundID()));
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                if (output.getCompound() instanceof CompoundRef.StoredOrVirtual c) {
                    target.add(new ExperimentReferencedCompound(ReactionRole.OUTPUT, c.getCompoundID()));
                }
            }
        }
    }
}
