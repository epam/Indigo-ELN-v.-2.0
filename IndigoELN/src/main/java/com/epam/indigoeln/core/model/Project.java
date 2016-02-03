package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection="project")
public class Project {

    @Id
    private String id;

    private String name;

    private List<String> tags;

    private List<String> keywords;

    private String references;

    private String description;

    //TODO may be need to use @DBRef
    private User author;

    //TODO may be need to use @DBRef. Also may be use accessList for this
    private List<User> users;

    @JsonIgnore
    @DBRef(lazy = true)
    private List<Notebook> notebooks = new ArrayList<>();

    private List<UserPermission> accessList = new ArrayList<>();

    @JsonIgnore
    private List<String> fileIds = new ArrayList<>();

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

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Notebook> getNotebooks() {
        return notebooks;
    }

    public void setNotebooks(List<Notebook> notebooks) {
        this.notebooks = notebooks;
    }

    public List<UserPermission> getAccessList() {
        return accessList;
    }

    public void setAccessList(List<UserPermission> accessList) {
        this.accessList = accessList;
    }

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }

//    public String getEditorMarkup() {
//        return editorMarkup;
//    }
//
//    public void setEditorMarkup(String editorMarkup) {
//        this.editorMarkup = editorMarkup;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        if (id != null ? !id.equals(project.id) : project.id != null) return false;
        if (name != null ? !name.equals(project.name) : project.name != null) return false;
        if (notebooks != null ? !notebooks.equals(project.notebooks) : project.notebooks != null) return false;
        if (accessList != null ? !accessList.equals(project.accessList) : project.accessList != null) return false;
        if (users != null ? !users.equals(project.users) : project.users != null) return false;
        if (author != null ? !author.equals(project.author) : project.author != null) return false;
        if (notebooks != null ? !notebooks.equals(project.notebooks) : project.notebooks != null) return false;
        if (keywords != null ? !keywords.equals(project.keywords) : project.keywords != null) return false;
        if (references != null ? !references.equals(project.references) : project.references != null)
            return false;
        return description != null ? description.equals(project.description) : project.description == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (accessList != null ? accessList.hashCode() : 0);
        result = 31 * result + (users != null ? users.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (notebooks != null ? notebooks.hashCode() : 0);
        result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
        result = 31 * result + (references != null ? references.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                "}";
    }
}