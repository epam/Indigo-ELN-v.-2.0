package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;

public class ExperimentShort extends BasicModelObject {

    private static final long serialVersionUID = 279335946658061099L;

    //    @NotBlank
    private String experimentNumber;

    private Long templateId;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
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
        if (!(o instanceof ExperimentShort)) return false;
        if (!super.equals(o)) return false;
        ExperimentShort that = (ExperimentShort) o;
        return  Objects.equal(experimentNumber, that.experimentNumber) &&
                Objects.equal(templateId, that.templateId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), experimentNumber, templateId);
    }

    @Override
    public String toString() {
        return "ExperimentShort{" +
                "experimentNumber='" + experimentNumber + '\'' +
                ", templateId='" + templateId + '\'' +
                "} " + super.toString();
    }
}
