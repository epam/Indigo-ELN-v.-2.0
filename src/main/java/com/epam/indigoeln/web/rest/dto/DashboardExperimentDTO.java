package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.ExperimentStatus;

import java.time.ZonedDateTime;
import java.util.List;

public class DashboardExperimentDTO {

    private String projectId;

    private String notebookId;

    private String experimentId;

    private String id;

    private String name;

    private ExperimentStatus status;

    private List<UserDTO> coAuthors;

    private String project;

    private ZonedDateTime creationDate;

    private ZonedDateTime lastEditDate;

    public DashboardExperimentDTO() {
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(String notebookId) {
        this.notebookId = notebookId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExperimentStatus getStatus() {
        return status;
    }

    public void setStatus(ExperimentStatus status) {
        this.status = status;
    }

    public List<UserDTO> getCoAuthors() {
        return coAuthors;
    }

    public void setCoAuthors(List<UserDTO> coAuthors) {
        this.coAuthors = coAuthors;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTime getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(ZonedDateTime lastEditDate) {
        this.lastEditDate = lastEditDate;
    }
}
