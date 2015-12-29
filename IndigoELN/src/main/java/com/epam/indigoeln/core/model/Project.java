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
}
