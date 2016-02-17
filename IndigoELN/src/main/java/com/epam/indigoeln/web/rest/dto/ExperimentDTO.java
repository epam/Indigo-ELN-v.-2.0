package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Experiment
 */
public class ExperimentDTO extends BasicDTO {

    private static final long serialVersionUID = -305591958439648518L;

    private TemplateDTO template;
    private List<ComponentDTO> components = new ArrayList<>();

    public ExperimentDTO() {
    }

    public ExperimentDTO(Experiment experiment) {
        super(experiment);
        if (experiment.getTemplate() != null) {
            this.template = new TemplateDTO(experiment.getTemplate());
        }
        this.components = experiment.getComponents() != null ?
                experiment.getComponents().stream().map(ComponentDTO::new).collect(Collectors.toList()) : new ArrayList<>();
    }

    public List<ComponentDTO> getComponents() {
        return components;
    }

    public TemplateDTO getTemplate() {
        return template;
    }

    public void setTemplate(TemplateDTO template) {
        this.template = template;
    }

    public void setComponents(List<ComponentDTO> components) {
        this.components = components != null ? components : new ArrayList<>();
    }
}
