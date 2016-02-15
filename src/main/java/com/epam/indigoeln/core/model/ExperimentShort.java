package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;

public class ExperimentShort extends BasicModelObject {

    private static final long serialVersionUID = 279335946658061099L;

    private String templateId;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperimentShort)) return false;
        if (!super.equals(o)) return false;
        ExperimentShort that = (ExperimentShort) o;
        return  Objects.equal(templateId, that.templateId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), templateId);
    }

    @Override
    public String toString() {
        return "ExperimentShort{" +
                ", templateId='" + templateId + '\'' +
                "} " + super.toString();
    }
}
