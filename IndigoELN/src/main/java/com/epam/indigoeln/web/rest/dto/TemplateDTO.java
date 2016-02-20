package com.epam.indigoeln.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.json.JSONArray;

import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.util.JsonUtil;

@JsonTypeName("Template")
public class TemplateDTO extends BasicDTO {

    private static final long serialVersionUID = 7951973267736585187L;

    @JsonProperty("templateContent")
    private Object[] templateContent;

    public TemplateDTO() {
    }

    public TemplateDTO(Template template) {
        super(template);
        this.templateContent = template.getTemplateContent() != null ? template.getTemplateContent().toArray() : null;
    }


    @JsonIgnore
    public JSONArray getTemplateContent() {
        return templateContent != null ? new JSONArray(templateContent) : null;
    }

    @JsonIgnore
    public void setTemplateContent(JSONArray templateContent) {
        this.templateContent = templateContent != null ? JsonUtil.jsonToArray(templateContent) : null;
    }
}
