package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;

public class ExperimentDescriptionModel implements SectionModel {
    private String description;

    public ExperimentDescriptionModel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
