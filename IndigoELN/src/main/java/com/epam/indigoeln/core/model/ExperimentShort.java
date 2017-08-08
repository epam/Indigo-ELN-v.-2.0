package com.epam.indigoeln.core.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExperimentShort extends BasicModelObject {

    private static final long serialVersionUID = 279335946658061099L;

    private Template template;

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }
}
