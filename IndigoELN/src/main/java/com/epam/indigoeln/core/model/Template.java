package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.util.JsonUtil;
import com.epam.indigoeln.core.util.LocalDateDeserializer;
import com.epam.indigoeln.core.util.LocalDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;
import com.mongodb.BasicDBList;
import org.hibernate.validator.constraints.NotBlank;
import org.json.JSONArray;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entity class presents Template
 */
@Document(collection = Template.COLLECTION_NAME)
public class Template implements Serializable, Persistable<String> {
    public static final String COLLECTION_NAME = "template";

    private static final long serialVersionUID = 4518230352458447262L;

    @Id
    private String id;

    @Version
    private Long version;

    @NotBlank
    @Indexed(unique = true)
    private String name;

    @CreatedDate
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate creationDate;

    @LastModifiedDate
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate lastEditDate;

    @CreatedBy
    private User createdBy;

    @LastModifiedBy
    private User lastModifiedBy;

    @Field("content")
    private BasicDBList templateContent;

    @Override
    public boolean isNew() {
        return id == null;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Long getVersion() {
        return version;
    }

    public BasicDBList getTemplateContent() {
        return templateContent;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
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
        if (o == null || getClass() != o.getClass()) return false;
        Template that = (Template) o;
        return  Objects.equal(id, that.id) &&
                Objects.equal(name, that.name) &&
                Objects.equal(templateContent, that.templateContent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, templateContent);
    }
}
