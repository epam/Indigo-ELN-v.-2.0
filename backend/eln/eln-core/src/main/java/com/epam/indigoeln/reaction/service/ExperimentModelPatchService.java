package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.handler.ExperimentModelValueHandler;
import com.epam.indigoeln.reaction.util.Flag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
public class ExperimentModelPatchService {

    public ExperimentModelPatch createPatch(ExperimentModel a, ExperimentModel b) {
        Flag updated = new Flag();
        //noinspection OptionalAssignedToNull,DataFlowIssue,OptionalGetWithoutIsPresent
        return ExperimentModelValueHandler.INSTANCE.compare(updated, a, b, null).get();
    }
}
