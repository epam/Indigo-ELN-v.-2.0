package com.epam.indigoeln.web.rest.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.epam.indigoeln.core.model.ExperimentTemplate;
import com.epam.indigoeln.core.util.LocalDateDeserializer;
import com.epam.indigoeln.core.util.LocalDateSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ExperimentTemplateDTO implements Serializable {

    private static final long serialVersionUID = 7951973267736585187L;

    private String id;
    private String name;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate creationDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate lastEditDate;

    private String createdBy;
    private String lastModifiedBy;
    private List<ComponentTemplateDTO> components = new ArrayList<>();

    public ExperimentTemplateDTO() {
    }

    public ExperimentTemplateDTO(ExperimentTemplate template) {
        this(template.getId(),
                template.getName(),
                template.getCreationDate(),
                template.getLastEditDate(),
                template.getCreatedBy() != null ? template.getCreatedBy().getLogin() : null,
                template.getLastModifiedBy() != null ? template.getLastModifiedBy().getLogin() : null,
                template.getComponents().stream().map(ComponentTemplateDTO::new).collect(Collectors.toList()));
    }

    public ExperimentTemplateDTO(String id,
                                 String name,
                                 LocalDate creationDate,
                                 LocalDate lastEditDate,
                                 String createdBy,
                                 String lastModifiedBy,
                                 List<ComponentTemplateDTO> components) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.lastEditDate = lastEditDate;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.components = components != null ? new ArrayList<>(components) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getLastEditDate() {
        return lastEditDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public List<ComponentTemplateDTO> getComponents() {
        return components;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComponents(List<ComponentTemplateDTO> components) {
        this.components = components != null ? new ArrayList<>(components) : new ArrayList<>();
    }
}
