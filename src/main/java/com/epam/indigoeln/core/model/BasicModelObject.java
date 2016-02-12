package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.util.LocalDateDeserializer;
import com.epam.indigoeln.core.util.LocalDateSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.base.Objects;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;

import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public abstract class BasicModelObject implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 315161850216225505L;

    @Id
    private String id;

    @NotNull
    private Long sequenceId;

    @NotEmpty
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
    private User author;

    @LastModifiedBy
    private User lastModifiedBy;

    private Set<UserPermission> accessList = new HashSet<>();

    public String getId() {
        return id;
    }

    public Long getSequenceId() {
        return sequenceId;
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

    public User getAuthor() {
        return author;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Set<UserPermission> getAccessList() {
        return accessList;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
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

    public void setAuthor(User author) {
        this.author = author;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void setAccessList(Set<UserPermission> accessList) {
        this.accessList = accessList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicModelObject)) return false;
        BasicModelObject that = (BasicModelObject) o;
        return  Objects.equal(id, that.id) &&
                Objects.equal(sequenceId, that.sequenceId) &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, sequenceId, name);
    }

    @Override
    public String toString() {
        return "BasicModelObject{" +
                "id='" + id + '\'' +
                ", sequenceId=" + sequenceId +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean isNew() {
        return getId() == null;
    }
}
