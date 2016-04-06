package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.util.SequenceIdUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public DashboardExperimentDTO(Experiment experiment, Notebook notebook, Project project) {
        this.projectId = SequenceIdUtil.extractShortId(project);
        this.notebookId = SequenceIdUtil.extractShortId(notebook);
        this.experimentId = SequenceIdUtil.extractShortId(experiment);
        this.id = experiment.getId();
        this.name = experiment.getName();
        this.status = experiment.getStatus();
        this.coAuthors =
                Optional.ofNullable(experiment.getCoAuthors()).orElse(new ArrayList<>())
                        .stream().map(UserDTO::new).collect(Collectors.toList());
        this.project = project.getName();
        this.creationDate = experiment.getCreationDate();
        this.lastEditDate = experiment.getLastEditDate();
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
