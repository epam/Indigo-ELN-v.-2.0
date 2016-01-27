package com.epam.indigoeln.web.rest.dto;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.json.JSONObject;

import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.util.LocalDateDeserializer;
import com.epam.indigoeln.core.util.LocalDateSerializer;
import com.epam.indigoeln.core.util.JSONObjectDeserializer;
import com.epam.indigoeln.core.util.JSONObjectSerializer;

public class TemplateDTO implements Serializable {

    private static final long serialVersionUID = 7951973267736585187L;

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

    @JsonSerialize(using = JSONObjectSerializer.class)
    @JsonDeserialize(using = JSONObjectDeserializer.class)
    private JSONObject templateContent;

    public TemplateDTO() {
    }

    public TemplateDTO(Template template) {
        this(template.getId(),
                template.getName(),
                template.getCreationDate(),
                template.getLastEditDate(),
                template.getCreatedBy() != null ? template.getCreatedBy().getLogin() : null,
                template.getLastModifiedBy() != null ? template.getLastModifiedBy().getLogin() : null,
                template.getTemplateContent() != null ?  new JSONObject(template.getTemplateContent().toString()) : null);
    }

    public TemplateDTO(String id,
                       String name,
                       LocalDate creationDate,
                       LocalDate lastEditDate,
                       String createdBy,
                       String lastModifiedBy,
                       JSONObject templateContent) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.lastEditDate = lastEditDate;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.templateContent = templateContent;
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

    public JSONObject getTemplateContent() {
        return templateContent;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTemplateContent(JSONObject templateContent) {
        this.templateContent = templateContent;
    }
}
