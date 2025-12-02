package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;
import com.epam.indigoeln.reaction.util.Flag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@Transactional
@ApplicationScoped
public class ExperimentModelPatchService {

    public ExperimentModelPatch createPatch(ExperimentModel a, ExperimentModel b) {
        Flag updated = new Flag();
        //noinspection OptionalAssignedToNull,DataFlowIssue,OptionalGetWithoutIsPresent
        return Handlers.EXPERIMENT_MODEL.compare(updated, a, b, null).get();
    }
}
