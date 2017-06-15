package com.epam.indigoeln.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.json.JSONArray;
import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.util.JsonUtil;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName("Template")
public class TemplateDTO extends BasicDTO {

    @JsonProperty("templateContent")
    private Object[] templateContent;

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateDTO.class);

    public TemplateDTO() {
        super();
    }

    public TemplateDTO(Template template) {
        super(template);
        this.templateContent = template.getTemplateContent() != null ? template.getTemplateContent().toArray() : null;
    }


    @JsonIgnore
    public JSONArray getTemplateContent() {
        try {
            return templateContent != null ? new JSONArray(templateContent) : null;
        } catch (JSONException e) {
            LOGGER.error("Get template content error",e);
            return null;
        }
    }

    @JsonIgnore
    public void setTemplateContent(JSONArray templateContent) {
        this.templateContent = templateContent != null ? JsonUtil.jsonToArray(templateContent) : null;
    }
}
