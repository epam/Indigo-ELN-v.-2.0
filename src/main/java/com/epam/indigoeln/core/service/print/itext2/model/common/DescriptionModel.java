package com.epam.indigoeln.core.service.print.itext2.model.common;

public class DescriptionModel implements SectionModel {
    private String description;
    private String entity;

    public DescriptionModel(String description, String entity) {
        this.description = description;
        this.entity = entity;
    }

    public String getDescription() {
        return description;
    }

    public String getEntity() {
        return entity;
    }
}
