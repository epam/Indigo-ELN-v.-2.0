package com.epam.indigoeln.core.model;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class ExperimentShort implements Serializable {

    private static final long serialVersionUID = 279335946658061099L;

    @Id
    protected String id;

    @NotBlank
    protected String experimentNumber;

    protected String title;

    @NotBlank
    protected String project;

    protected String templateId;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getExperimentNumber() {
        return experimentNumber;
    }

    public void setExperimentNumber(String experimentNumber) {
        this.experimentNumber = experimentNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentShort that = (ExperimentShort) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        if (templateId != null ? !templateId.equals(that.templateId) : that.templateId != null) return false;

        return true;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (templateId != null ? templateId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExperimentShort{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", project='" + project + '\'' +
                ", templateId='" + templateId + '\'' +
                ", experimentNumber='" + experimentNumber + '\'' +
                "}";
    }
}
