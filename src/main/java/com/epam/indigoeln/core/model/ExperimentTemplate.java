package com.epam.indigoeln.core.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.epam.indigoeln.core.util.LocalDateDeserializer;
import com.epam.indigoeln.core.util.LocalDateSerializer;

/**
 * Entity class presents ExperimentTemplate
 */
@Document(collection = "template")
public class ExperimentTemplate implements Serializable, Persistable<String> {

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

    @DBRef
    private List<ComponentTemplate> components = new ArrayList<>();

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

    public List<ComponentTemplate> getComponents() {
        return components;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComponents(List<ComponentTemplate> components) {
        this.components = components != null ? new ArrayList<>(components) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentTemplate that = (ExperimentTemplate) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return components != null ? components.equals(that.components) : that.components == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (components != null ? components.hashCode() : 0);
        return result;
    }
}
