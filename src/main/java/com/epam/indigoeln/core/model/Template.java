package com.epam.indigoeln.core.model;

import java.io.Serializable;
import java.time.LocalDate;

import org.hibernate.validator.constraints.NotBlank;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.epam.indigoeln.core.util.LocalDateDeserializer;
import com.epam.indigoeln.core.util.LocalDateSerializer;

/**
 * Entity class presents Template
 */
@Document(collection = "template")
public class Template implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 4518230352458447262L;

    @Id
    private String id;

    @Version
    private Long version;

    @NotBlank
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
    private String templateContent;

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

    public String getTemplateContent() {
        return templateContent;
    }

    public Long getVersion() {
        return version;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastEditDate(LocalDate lastEditDate) {
        this.lastEditDate = lastEditDate;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Template template = (Template) o;

        if (id != null ? !id.equals(template.id) : template.id != null) return false;
        if (name != null ? !name.equals(template.name) : template.name != null) return false;
        if (creationDate != null ? !creationDate.equals(template.creationDate) : template.creationDate != null)
            return false;
        if (lastEditDate != null ? !lastEditDate.equals(template.lastEditDate) : template.lastEditDate != null)
            return false;
        if (createdBy != null ? !createdBy.equals(template.createdBy) : template.createdBy != null) return false;
        if (lastModifiedBy != null ? !lastModifiedBy.equals(template.lastModifiedBy) : template.lastModifiedBy != null)
            return false;
        return templateContent != null ? templateContent.equals(template.templateContent) : template.templateContent == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (lastEditDate != null ? lastEditDate.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        result = 31 * result + (templateContent != null ? templateContent.hashCode() : 0);
        return result;
    }
}
