/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.User;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Experiment.
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
    private String experimentFullName;

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
        this.components = experiment.getComponents() != null
                ? experiment.getComponents().stream().map(ComponentDTO::new).collect(Collectors.toList())
                : new ArrayList<>();
        this.status = experiment.getStatus();
        this.documentId = experiment.getDocumentId();
        this.experimentVersion = experiment.getExperimentVersion();
        this.lastVersion = experiment.isLastVersion();
        this.fullName = experiment.getFullName();
        this.experimentFullName = experiment.getExperimentFullName();
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

    public String getExperimentFullName() {
        return experimentFullName;
    }

    public void setExperimentFullName(String experimentFullName) {
        this.experimentFullName = experimentFullName;
    }
}
