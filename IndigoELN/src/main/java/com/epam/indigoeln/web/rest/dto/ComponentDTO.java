package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Component;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mongodb.BasicDBObject;

@JsonTypeName("Component")
public class ComponentDTO {

    private String id;
    private String name;
    private BasicDBObject content;

    public ComponentDTO() {
        super();
    }

    public ComponentDTO(Component component) {
        if (component != null) {
            this.id = component.getId();
            this.content = component.getContent();
            this.name = component.getName();
        }
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

    public BasicDBObject getContent() {
        return content;
    }

    public void setContent(BasicDBObject content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ComponentDTO{" +
                "id='" + id + '\'' +
                ", content=" + content +
                '}';
    }
}
