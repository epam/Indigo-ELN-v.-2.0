package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Experiment;

import java.io.Serializable;

/**
 * DTO for Experiment
 * Does not contain embedded fields, such as Author, Batch, etc. // TODO Need to supplement
 */
public class ExperimentDTO implements Serializable {

    private static final long serialVersionUID = -305591958439648518L;

    private String id;
    private String title;
    private String project;
    private String experimentNumber;
    private String templateId;

    public ExperimentDTO() {
    }

    public ExperimentDTO(Experiment experiment) {
        this.id =experiment.getId();
        this.title =experiment.getTitle();
        this.project =experiment.getProject();
        this.experimentNumber =experiment.getExperimentNumber();
        this.templateId =experiment.getTemplateId();
    }

    public String getId() {
        return id;
    }

    public String getExperimentNumber() {
        return experimentNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getProject() {
        return project;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExperimentNumber(String experimentNumber) {
        this.experimentNumber = experimentNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
