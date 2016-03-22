package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Component;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mongodb.BasicDBObject;

import java.io.Serializable;

@JsonTypeName("Component")
public class ComponentDTO implements Serializable {

    private static final long serialVersionUID = 13994253258215137L;

    private String id;
    private String name;
    private BasicDBObject content;

    public ComponentDTO() {
        super();
    }

    public ComponentDTO(Component component) {
        this.id = component.getId();
        this.content = component.getContent();
        this.name = component.getName();
    }

    public String getId() {
        return id;
    }

    public BasicDBObject getContent() {
        return content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(BasicDBObject content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ComponentDTO{" +
                "id='" + id + '\'' +
                ", content=" + content +
                '}';
    }
}
