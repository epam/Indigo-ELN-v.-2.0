package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;
import com.epam.indigoeln.reaction.util.Flag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@Transactional
@ApplicationScoped
public class ExperimentModelPatchService {

    public ExperimentPatch createPatch(ExperimentSnapshot a, ExperimentSnapshot b) {
        Flag updated = new Flag();
        //noinspection OptionalAssignedToNull,DataFlowIssue,OptionalGetWithoutIsPresent
        return Handlers.EXPERIMENT.compare(updated, a, b, null).get();
    }
}
