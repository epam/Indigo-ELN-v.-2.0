package com.epam.indigoeln.web.rest.dto;

import java.io.Serializable;
import com.epam.indigoeln.core.model.Component;
import com.mongodb.BasicDBObject;

public class ComponentDTO implements Serializable {

    private static final long serialVersionUID = 13994253258215137L;

    private String id;
    private BasicDBObject content;
    private String componentTemplateId;

    public ComponentDTO() {
    }

    public ComponentDTO(Component component) {
        this(component.getId(),
             component.getContent(),
             component.getComponentTemplateId());
    }

    public ComponentDTO(String id,
                        BasicDBObject content,
                        String componentTemplateId) {
        this.id = id;
        this.content = content;
        this.componentTemplateId = componentTemplateId;
    }

    public String getId() {
        return id;
    }

    public BasicDBObject getContent() {
        return content;
    }

    public String getComponentTemplateId() {
        return componentTemplateId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(BasicDBObject content) {
        this.content = content;
    }

    public void setComponentTemplateId(String componentTemplateId) {
        this.componentTemplateId = componentTemplateId;
    }

    @Override
    public String toString() {
        return "ComponentDTO{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", componentTemplateId='" + componentTemplateId + '\'' +
                '}';
    }
}
