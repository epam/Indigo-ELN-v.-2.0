package com.epam.indigoeln.core.service.print.itext2.model.project;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;

public class ProjectSummaryModel implements SectionModel {
    private String keywords;
    private String literature;

    public ProjectSummaryModel(String keywords, String literature) {
        this.keywords = keywords;
        this.literature = literature;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getLiterature() {
        return literature;
    }
}
