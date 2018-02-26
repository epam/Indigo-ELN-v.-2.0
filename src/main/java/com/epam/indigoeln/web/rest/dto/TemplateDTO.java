/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.json.JSONArray;
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
            LOGGER.error("Get template content error", e);
            return null;
        }
    }

    @JsonIgnore
    public void setTemplateContent(JSONArray templateContent) {
        this.templateContent = templateContent != null ? JsonUtil.jsonToArray(templateContent) : null;
    }
}
