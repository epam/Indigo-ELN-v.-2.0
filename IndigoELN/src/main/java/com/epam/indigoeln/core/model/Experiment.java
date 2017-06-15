package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = Experiment.COLLECTION_NAME)
public class Experiment extends ExperimentShort {
    public static final String COLLECTION_NAME = "experiment";

    private static final long serialVersionUID = -4215770980241351691L;

    //TODO may be need to use @DBRef
    private List<User> coAuthors;

    //TODO may be need to use @DBRef
    private List<User> witness;

    private String comments;

    private ExperimentStatus status;

    private String documentId;

    private User submittedBy;

    private int experimentVersion;

    private boolean lastVersion;

    @JsonIgnore
    @DBRef
    private List<Component> components;

    @JsonIgnore
    private Set<String> fileIds = new HashSet<>();

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

    public ExperimentStatus getStatus() {
        return status;
    }

    public void setStatus(ExperimentStatus status) {
        this.status = status;
    }

    public List<User> getWitness() {
        return witness;
    }

    public void setWitness(List<User> witness) {
        this.witness = witness;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public Set<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(Set<String> fileIds) {
        this.fileIds = fileIds;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public int getExperimentVersion() {
        return experimentVersion;
    }

    public void setExperimentVersion(int experimentVersion) {
        this.experimentVersion = experimentVersion;
    }

    public boolean isLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(boolean lastVersion) {
        this.lastVersion = lastVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Experiment)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Experiment that = (Experiment) o;
        boolean equalCoAuthorsAndWitnesses =
                Objects.equal(coAuthors, that.coAuthors) &&
                        Objects.equal(witness, that.witness);
        return equalCoAuthorsAndWitnesses &&
                Objects.equal(comments, that.comments) &&
                Objects.equal(status, that.status) &&
                Objects.equal(components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), coAuthors, witness, comments, status, components);
    }

    @Override
    public String toString() {
        return "Experiment{" +
                "status='" + status + '\'' +
                ", comments='" + comments + '\'' +
                "} " + super.toString();
    }
}