package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Experiment;

/**
 * A DTO for representing Experiment like a Tree Node with its properties
 */
public class ExperimentTreeNodeDTO extends TreeNodeDTO {

    private String status;

    public ExperimentTreeNodeDTO() {
        super();
    }

    public ExperimentTreeNodeDTO(Experiment experiment) {
        super(experiment);
        this.status = experiment.getStatus().toString();
        if (experiment.getExperimentVersion() > 1 || !experiment.isLastVersion()) {
            setName(getName() + " v" + experiment.getExperimentVersion());
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}