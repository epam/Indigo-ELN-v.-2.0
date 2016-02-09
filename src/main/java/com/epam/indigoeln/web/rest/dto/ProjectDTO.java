package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for Project
 */
public class ProjectDTO implements Serializable {

    private static final long serialVersionUID = -4754176248352140289L;

    @JsonProperty("id")
    private Long sequenceId;

    private String name;
    private List<String> tags;
    private List<String> keywords;
    private String references;
    private String description;
    private User author;
    private List<NotebookDTO> notebooks;
    private Set<String> fileIds;
    private Set<UserPermission> accessList;

    public ProjectDTO() {
    }

    public ProjectDTO(Project project) {
        this.sequenceId = project.getSequenceId();
        this.name = project.getName();
        this.accessList = project.getAccessList();
        this.author = project.getAuthor();
        this.description = project.getDescription();
        this.fileIds = project.getFileIds();
        this.keywords = project.getKeywords();
        this.notebooks = project.getNotebooks() != null ?
                project.getNotebooks().stream().map(NotebookDTO::new).collect(Collectors.toList()) : new ArrayList<>();
        this.references = project.getReferences();
        this.tags = project.getTags();
    }

    public Set<UserPermission> getAccessList() {
        return accessList;
    }

    public void setAccessList(Set<UserPermission> accessList) {
        this.accessList = accessList;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
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

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long id) {
        this.sequenceId = id;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "ProjectDTO{" +
                "name='" + name + "\'" +
                "}";
    }
}