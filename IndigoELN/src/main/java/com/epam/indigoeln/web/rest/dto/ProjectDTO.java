package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for Project
 */
public class ProjectDTO extends BasicDTO {

    private static final long serialVersionUID = -4754176248352140289L;

    private List<String> tags;
    private List<String> keywords;
    private String references;
    private String description;
    private List<NotebookDTO> notebooks;
    private Set<String> fileIds;

    public ProjectDTO() {
    }

    public ProjectDTO(Project project) {

        super(project.getSequenceId(),
              project.getName(),
              project.getAccessList(),
              new UserDTO(project.getAuthor()),
              new UserDTO(project.getLastModifiedBy()),
              project.getCreationDate(),
              project.getLastEditDate());

        this.description = project.getDescription();
        this.fileIds = project.getFileIds();
        this.keywords = project.getKeywords();
        this.notebooks = project.getNotebooks() != null ?
                project.getNotebooks().stream().map(NotebookDTO::new).collect(Collectors.toList()) : new ArrayList<>();
        this.references = project.getReferences();
        this.tags = project.getTags();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(Set<String> fileIds) {
        this.fileIds = fileIds;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<NotebookDTO> getNotebooks() {
        return notebooks;
    }

    public void setNotebooks(List<NotebookDTO> notebooks) {
        this.notebooks = notebooks;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "ProjectDTO{} " + super.toString();
    }
}