package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;
import org.springframework.data.annotation.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ExperimentShort implements Serializable {

    private static final long serialVersionUID = 279335946658061099L;

    @Id
    private String id;

    @NotNull
    private Long sequenceId;

    //    @NotBlank
    private String experimentNumber;

    private String title;

    //    @NotBlank
    private String project;

    private String templateId;

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

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperimentShort)) return false;
        ExperimentShort that = (ExperimentShort) o;
        return  Objects.equal(id, that.id) &&
                Objects.equal(sequenceId, that.sequenceId) &&
                Objects.equal(title, that.title) &&
                Objects.equal(project, that.project) &&
                Objects.equal(templateId, that.templateId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, sequenceId, title, project, templateId);
    }

    @Override
    public String toString() {
        return "ExperimentShort{" +
                "id='" + id + '\'' +
                ", sequenceId=" + sequenceId +
                ", experimentNumber='" + experimentNumber + '\'' +
                ", title='" + title + '\'' +
                ", project='" + project + '\'' +
                ", templateId='" + templateId + '\'' +
                '}';
    }
}
