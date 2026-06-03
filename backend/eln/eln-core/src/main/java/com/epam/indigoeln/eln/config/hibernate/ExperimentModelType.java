package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.ExperimentModel;

public class ExperimentModelType extends AbstractJsonUserType<ExperimentModel> {

    ExperimentModelType() {
        super(ExperimentModel.class);
    }

    @Override
    public boolean isMutable() {
        return true;
    }
}
