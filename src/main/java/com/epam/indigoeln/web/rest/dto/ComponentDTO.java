package com.epam.indigoeln.web.rest.dto;

import java.io.Serializable;
import com.epam.indigoeln.core.model.Component;

public class ComponentDTO implements Serializable {

    private static final long serialVersionUID = 13994253258215137L;

    private String id;
    private String componentNumber;
    private String jsonContent;
    private String molfile;
    private String componentTemplateId;

    public ComponentDTO() {
    }

    public ComponentDTO(Component component) {
        this(component.getId(),
             component.getComponentNumber(),
             component.getJsonContent(),
             component.getComponentTemplateId());
    }

    public ComponentDTO(String id,
                        String componentNumber,
                        String jsonContent,
                        String componentTemplateId) {
        this.id = id;
        this.componentNumber = componentNumber;
        this.jsonContent = jsonContent;
        this.componentTemplateId = componentTemplateId;
    }

    public String getId() {
        return id;
    }

    public String getComponentNumber() {
        return componentNumber;
    }

    public String getMolfile() {
        return molfile;
    }

    public String getJsonContent() {
        return jsonContent;
    }

    public String getComponentTemplateId() {
        return componentTemplateId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setComponentNumber(String componentNumber) {
        this.componentNumber = componentNumber;
    }

    public void setMolfile(String molfile) {
        this.molfile = molfile;
    }

    public void setJsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
    }

    public void setComponentTemplateId(String componentTemplateId) {
        this.componentTemplateId = componentTemplateId;
    }

    @Override
    public String toString() {
        return "ComponentDTO{" +
                "id='" + id + '\'' +
                ", componentNumber='" + componentNumber + '\'' +
                ", jsonContent='" + jsonContent + '\'' +
                ", molfile='" + molfile + '\'' +
                ", componentTemplateId='" + componentTemplateId + '\'' +
                '}';
    }
}
