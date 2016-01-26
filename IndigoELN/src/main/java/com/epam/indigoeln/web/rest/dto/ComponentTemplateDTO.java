package com.epam.indigoeln.web.rest.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.epam.indigoeln.core.model.ComponentTemplate;
import com.epam.indigoeln.core.util.LocalDateDeserializer;
import com.epam.indigoeln.core.util.LocalDateSerializer;

public class ComponentTemplateDTO implements Serializable {

    private static final long serialVersionUID = 7244336182712916004L;

    private String id;
    private String name;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate creationDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate lastEditDate;

    private String createdBy;
    private String lastModifiedBy;
    private String templateContent;

    public ComponentTemplateDTO() {
    }

    public ComponentTemplateDTO(String id, String name, LocalDate creationDate, LocalDate lastEditDate,
                                String createdBy, String lastModifiedBy, String templateContent) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.lastEditDate = lastEditDate;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.templateContent = templateContent;
    }

    public ComponentTemplateDTO(ComponentTemplate template) {
        this(template.getId(),
             template.getName(),
             template.getCreationDate(),
             template.getLastEditDate(),
             template.getCreatedBy()!= null ? template.getCreatedBy().getLogin() : null,
             template.getLastModifiedBy() != null ? template.getLastModifiedBy().getLogin() : null,
             template.getTemplateContent());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getLastEditDate() {
        return lastEditDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    @Override
    public String toString() {
        return "ComponentTemplateDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", templateContent='" + templateContent + '\'' +
                '}';
    }
}
