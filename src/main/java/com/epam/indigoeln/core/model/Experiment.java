package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.util.LocalDateDeserializer;
import com.epam.indigoeln.core.util.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = Experiment.COLLECTION_NAME)
public class Experiment extends ExperimentShort {
    public static final String COLLECTION_NAME = "experiment";

    private static final long serialVersionUID = -4215770980241351691L;

    @Version
    private Long version;

    //TODO may be need to use @DBRef
    private User author;

    //TODO may be need to use @DBRef
    private List<User> coAuthors;

    //TODO may be need to use @DBRef
    private List<User> witness;

    @CreatedDate
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate creationDate;

    @LastModifiedDate
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate lastEditDate;

    @LastModifiedBy
    private User lastModifiedBy;

    private String comments;

    // TODO enum: open, completed, submitted, submit_fail, signing, archived
    private String status;

    @JsonIgnore
    @DBRef
    private List<Component> components;

    private Set<UserPermission> accessList = new HashSet<>();

    @JsonIgnore
    private Set<String> fileIds = new HashSet<>();

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<User> getCoAuthors() {
        return coAuthors;
    }

    public void setCoAuthors(List<User> coAuthors) {
        this.coAuthors = coAuthors;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<User> getWitness() {
        return witness;
    }

    public void setWitness(List<User> witness) {
        this.witness = witness;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDate getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(LocalDate lastEditDate) {
        this.lastEditDate = lastEditDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public Long getVersion() {
        return version;
    }

    public Set<UserPermission> getAccessList() {
        return accessList;
    }

    public void setAccessList(Set<UserPermission> accessList) {
        this.accessList = accessList;
    }

    public Set<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(Set<String> fileIds) {
        this.fileIds = fileIds;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Experiment)) return false;
        if (!super.equals(o)) return false;
        Experiment that = (Experiment) o;
        return  Objects.equal(author, that.author) &&
                Objects.equal(coAuthors, that.coAuthors) &&
                Objects.equal(witness, that.witness) &&
                Objects.equal(creationDate, that.creationDate) &&
                Objects.equal(lastEditDate, that.lastEditDate) &&
                Objects.equal(lastModifiedBy, that.lastModifiedBy) &&
                Objects.equal(comments, that.comments) &&
                Objects.equal(status, that.status) &&
                Objects.equal(components, that.components) &&
                Objects.equal(accessList, that.accessList);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), author, coAuthors, witness, creationDate, lastEditDate,
                lastModifiedBy, comments, status, components, accessList);
    }

    @Override
    public String toString() {
        return "Experiment{" +
                "status='" + status + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", lastEditDate='" + lastEditDate + '\'' +
                ", comments='" + comments + '\'' +
                "} " + super.toString();
    }


}