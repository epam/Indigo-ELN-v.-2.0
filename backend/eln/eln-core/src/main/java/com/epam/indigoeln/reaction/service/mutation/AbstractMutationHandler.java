package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import lombok.Setter;

@Setter
public abstract class AbstractMutationHandler {

    protected ExperimentEntity experiment;
    protected ExperimentModel model;
}
