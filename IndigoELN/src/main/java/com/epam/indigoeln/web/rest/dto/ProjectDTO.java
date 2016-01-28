package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import java.io.Serializable;
import java.util.List;

/**
 * DTO for Project
 */
public class ProjectDTO implements Serializable {

    private String id;
    private String name;
    private List<String> tags;
    private List<String> keywords;
    private String references;
    private String description;
    private User author;
    private List<User> users;
    private List<Notebook> notebooks;
    private List<String> fileIds;
    private List<UserPermission> accessList;
//    private String editorMarkup;

    public ProjectDTO() {
    }

    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.accessList = project.getAccessList();
        this.author = project.getAuthor();
        this.description = project.getDescription();
        this.fileIds = project.getFileIds();
        this.keywords = project.getKeywords();
        this.notebooks = project.getNotebooks();
        this.references = project.getReferences();
        this.tags = project.getTags();
        this.users = project.getUsers();
    }

    public List<UserPermission> getAccessList() {
        return accessList;
    }

    public void setAccessList(List<UserPermission> accessList) {
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

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<Notebook> getNotebooks() {
        return notebooks;
    }

    public void setNotebooks(List<Notebook> notebooks) {
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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "ProjectDTO{" +
                "name='" + name + "\'" +
                "}";
    }
}