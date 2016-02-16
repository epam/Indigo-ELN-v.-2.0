package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.Persistable;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

public abstract class BasicModelObject implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 315161850216225505L;

    @Id
    private String id;

    @NotEmpty
    private String name;

    @CreatedDate
    private ZonedDateTime creationDate = ZonedDateTime.now();
    ;

    @LastModifiedDate
    private ZonedDateTime lastEditDate = ZonedDateTime.now();

    @CreatedBy
    private User author;

    @LastModifiedBy
    private User lastModifiedBy;

    private Set<UserPermission> accessList = new HashSet<>();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public ZonedDateTime getLastEditDate() {
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
    public void setName(String name) {
        this.name = name;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastEditDate(ZonedDateTime lastEditDate) {
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
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name);
    }

    @Override
    public String toString() {
        return "BasicModelObject{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean isNew() {
        return getId() == null;
    }

    public String getShortId() {
        if(id == null) return null;
        String[] split = id.split("-");
        return split[split.length - 1];
    }
}
