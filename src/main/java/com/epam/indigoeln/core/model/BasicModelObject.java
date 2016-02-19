package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;

import org.hibernate.validator.constraints.NotEmpty;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;

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

    private String title;

    @CreatedDate
    private ZonedDateTime creationDate = ZonedDateTime.now();

    @LastModifiedDate
    private ZonedDateTime lastEditDate = ZonedDateTime.now();

    @CreatedBy
    private User author;

    @LastModifiedBy
    private User lastModifiedBy;

    @Version
    private Long version;

    private Set<UserPermission> accessList = new HashSet<>();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
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

    public Long getVersion() {
        return version;
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

    public void setTitle(String title) {
        this.title = title;
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
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public boolean isNew() {
        return Long.valueOf(0L).equals(version);
    }
}
