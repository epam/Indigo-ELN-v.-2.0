package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;

public class ExperimentShort extends BasicModelObject {

    private static final long serialVersionUID = 279335946658061099L;

    private Template template;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExperimentShort)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ExperimentShort that = (ExperimentShort) o;
        return Objects.equal(template, that.template);
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), template);
    }

    @Override
    public String toString() {
        return "ExperimentShort{" +
                ", templateId='" + template + '\'' +
                "} " + super.toString();
    }
}
