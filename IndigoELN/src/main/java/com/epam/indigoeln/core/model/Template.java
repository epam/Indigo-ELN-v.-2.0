package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.util.JsonUtil;
import com.mongodb.BasicDBList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.json.JSONArray;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


/**
 * Entity class presents Template
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Document(collection = Template.COLLECTION_NAME)
public class Template extends BasicModelObject {

    public static final String COLLECTION_NAME = "template";

    private static final long serialVersionUID = 4518230352458447262L;


    @Field("content")
    private BasicDBList templateContent;

    public BasicDBList getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(BasicDBList templateContent) {
        this.templateContent = templateContent;
    }

    public void setTemplateContent(JSONArray templateContent) {
        this.templateContent = templateContent != null ? JsonUtil.basicDBListFromJsonArray(templateContent) : null;
    }
}
