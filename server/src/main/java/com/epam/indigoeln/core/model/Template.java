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
package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.util.JsonUtil;
import com.mongodb.BasicDBList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.json.JSONArray;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;


/**
 * Entity class presents Template.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = Template.COLLECTION_NAME)
public class Template extends BasicModelObject {

    public static final String COLLECTION_NAME = "template";

    private static final long serialVersionUID = 4518230352458447262L;


    @Field("content")
    private List<Object> templateContent;

    public List<Object> getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(List<Object> templateContent) {
        this.templateContent = templateContent;
    }

    public void setTemplateContent(JSONArray templateContent) {
        this.templateContent = templateContent != null ? JsonUtil.basicDBListFromJsonArray(templateContent) : null;
    }
}
