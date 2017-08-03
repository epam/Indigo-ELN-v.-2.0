package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.User;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Experiment
 */
@JsonTypeName("Experiment")
public class ExperimentDTO extends BasicDTO {

    private static final long serialVersionUID = -305591958439648518L;

    private TemplateDTO template;
    private List<ComponentDTO> components = new ArrayList<>();
    private ExperimentStatus status;
    private String documentId;
    private User submittedBy;
    private String fullName;

    private int experimentVersion;
    private boolean lastVersion;

    public ExperimentDTO() {
        super();
    }

    public ExperimentDTO(Experiment experiment) {
        super(experiment);
        if (experiment.getTemplate() != null) {
            this.template = new TemplateDTO(experiment.getTemplate());
        }
        this.components = experiment.getComponents() != null ?
                experiment.getComponents().stream().map(ComponentDTO::new).collect(Collectors.toList()) : new ArrayList<>();
        this.status = experiment.getStatus();
        this.documentId = experiment.getDocumentId();
        this.experimentVersion = experiment.getExperimentVersion();
        this.lastVersion = experiment.isLastVersion();
        this.fullName = experiment.getFullName();
    }

    public List<ComponentDTO> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentDTO> components) {
        this.components = components != null ? components : new ArrayList<>();
    }

    public TemplateDTO getTemplate() {
        return template;
    }

    public void setTemplate(TemplateDTO template) {
        this.template = template;
    }

    public ExperimentStatus getStatus() {
        return status;
    }

    public void setStatus(ExperimentStatus status) {
        this.status = status;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public int getExperimentVersion() {
        return experimentVersion;
    }

    public void setExperimentVersion(int experimentVersion) {
        this.experimentVersion = experimentVersion;
    }

    public boolean isLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(boolean lastVersion) {
        this.lastVersion = lastVersion;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
