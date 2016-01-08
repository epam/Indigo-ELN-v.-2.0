package com.epam.indigoeln.core.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "experiment")
public class Experiment implements Serializable {

    @Id
    private String id;

    private String title;

    private User author;

    private List<User> coAuthors;

    private List<User> witness;

    private String project;

    @CreatedDate
    private LocalDate creationDate;

    @LastModifiedDate
    private LocalDate lastEditDate;

    @LastModifiedBy
    private User lastModifiedBy;

    private String comments;

    // TODO enum: open, completed, submitted, submit_fail, signing, archived
    private String status;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Experiment that = (Experiment) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (coAuthors != null ? !coAuthors.equals(that.coAuthors) : that.coAuthors != null) return false;
        if (witness != null ? !witness.equals(that.witness) : that.witness != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (lastEditDate != null ? !lastEditDate.equals(that.lastEditDate) : that.lastEditDate != null) return false;
        return comments != null ? comments.equals(that.comments) : that.comments == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (coAuthors != null ? coAuthors.hashCode() : 0);
        result = 31 * result + (witness != null ? witness.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (lastEditDate != null ? lastEditDate.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        return result;
    }
}
