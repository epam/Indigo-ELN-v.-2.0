package com.epam.indigoeln.core.service.print.itext2.model.project;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;

public class ProjectSummaryModel implements SectionModel {
    private String keywords;
    private String literature;
    private String description;

    public ProjectSummaryModel(String keywords, String literature, String description) {
        this.keywords = keywords;
        this.literature = literature;
        this.description = description;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getLiterature() {
        return literature;
    }

    public String getDescription() {
        return description;
    }
}
