package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Experiment;
import com.mongodb.DBObject;
import lombok.val;

/**
 * A DTO for representing Experiment like a Tree Node with its properties.
 */
public class ExperimentTreeNodeDTO extends TreeNodeDTO {

    private String status;

    public ExperimentTreeNodeDTO() {
        super();
    }

    public ExperimentTreeNodeDTO(Experiment experiment) {
        super(experiment);
        this.status = experiment.getStatus().toString();
        setName(experiment.getFullName());
    }

    public ExperimentTreeNodeDTO(DBObject obj) {
        super(obj);
        this.status = String.valueOf(obj.get("status"));

        val experimentName = String.valueOf(obj.get("name"));
        val experimentVersion = Integer.parseInt(String.valueOf(obj.get("experimentVersion")));
        val lastVersion = Boolean.parseBoolean(String.valueOf(obj.get("lastVersion")));

        if (experimentVersion > 1 || !lastVersion) {
            setName(experimentName + " v" + experimentVersion);
        } else {
            setName(experimentName);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
