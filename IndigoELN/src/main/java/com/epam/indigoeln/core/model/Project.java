package com.epam.indigoeln.core.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection="project")
public class Project {
    @Id
    private String id;

    @Field("projectName")
    private String name;

    @Field("projectUsers")
    private List<String> users;

    @Field("projectNotebooks")
    private List<String> notebooks;

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

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getNotebooks() {
        return notebooks;
    }

    public void setNotebooks(List<String> notebooks) {
        this.notebooks = notebooks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;

        Project project = (Project) o;

        if (id != null ? !id.equals(project.id) : project.id != null) return false;
        if (name != null ? !name.equals(project.name) : project.name != null) return false;
        if (notebooks != null ? !notebooks.equals(project.notebooks) : project.notebooks != null) return false;
        if (users != null ? !users.equals(project.users) : project.users != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (users != null ? users.hashCode() : 0);
        result = 31 * result + (notebooks != null ? notebooks.hashCode() : 0);
        return result;
    }
}
