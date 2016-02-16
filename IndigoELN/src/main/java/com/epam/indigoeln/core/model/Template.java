package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.util.JsonUtil;
import com.google.common.base.Objects;
import com.mongodb.BasicDBList;
import org.json.JSONArray;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


/**
 * Entity class presents Template
 */
@Document(collection = Template.COLLECTION_NAME)
public class Template extends BasicModelObject  {

    public static final String COLLECTION_NAME = "template";

    private static final long serialVersionUID = 4518230352458447262L;

    @Version
    private Long version;


    @Field("content")
    private BasicDBList templateContent;

    public Long getVersion() {
        return version;
    }

    public BasicDBList getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(BasicDBList templateContent) {
        this.templateContent = templateContent;
    }

    public void setTemplateContent(JSONArray templateContent) {
        this.templateContent = templateContent != null ? JsonUtil.basicDBListFromJsonArray(templateContent) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Template)) return false;
        if (!super.equals(o)) return false;
        Template template = (Template) o;
        return Objects.equal(templateContent, template.templateContent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), templateContent);
    }

    @Override
    public String toString() {
        return "Template{" +
                "templateContent=" + templateContent +
                "} " + super.toString();
    }
}
