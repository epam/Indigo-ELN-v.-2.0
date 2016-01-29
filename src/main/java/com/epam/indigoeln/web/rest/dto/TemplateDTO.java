package com.epam.indigoeln.web.rest.dto;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.json.JSONArray;

import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.util.LocalDateDeserializer;
import com.epam.indigoeln.core.util.LocalDateSerializer;
import com.epam.indigoeln.core.util.JsonUtil;

public class TemplateDTO implements Serializable {

    private static final long serialVersionUID = 7951973267736585187L;

    private String id;
    private String name;

    @JsonProperty("creationDate")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate creationDate;

    @JsonProperty("lastEditDate")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate lastEditDate;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy;

    @JsonProperty("templateContent")
    private Object[] templateContent;

    public TemplateDTO() {
    }

    public TemplateDTO(Template template) {
        this.id              = template.getId();
        this.name            = template.getName();
        this.creationDate    = template.getCreationDate();
        this.lastEditDate    = template.getLastEditDate();
        this.createdBy       = template.getCreatedBy() != null ? template.getCreatedBy().getLogin() : null;
        this.lastModifiedBy  = template.getLastModifiedBy() != null ? template.getLastModifiedBy().getLogin() : null;
        this.templateContent = template.getTemplateContent() != null ? template.getTemplateContent().toArray() : null;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public JSONArray getTemplateContent() {
        return templateContent != null ? new JSONArray(templateContent) : null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public void setTemplateContent(JSONArray templateContent) {
        this.templateContent = templateContent != null ? JsonUtil.jsonToArray(templateContent) : null;
    }

}
