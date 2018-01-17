package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = Experiment.COLLECTION_NAME)
public class Experiment extends ExperimentShort {
    public static final String COLLECTION_NAME = "experiment";

    private static final long serialVersionUID = -4215770980241351691L;

    private String experimentFullName;

    private String comments;

    private ExperimentStatus status;

    private String documentId;

    @DBRef
    private User submittedBy;

    private int experimentVersion;

    private boolean lastVersion;

    @JsonIgnore
    @DBRef
    private List<Component> components;

    @JsonIgnore
    private transient Set<String> fileIds = new HashSet<>();

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

    public String getExperimentFullName() {
        return experimentFullName;
    }

    public void setExperimentFullName(String experimentFullName) {
        this.experimentFullName = experimentFullName;
    }

    public void compileExperimentFullName(String notebookName){
        this.experimentFullName = notebookName + "-" + getFullName();
    }

    public String getFullName() {
        if (experimentVersion > 1 || !lastVersion) {
            return getName() + " v" + getExperimentVersion();
        } else {
            return getName();
        }
    }
}
