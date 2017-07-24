package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;

import java.util.List;

public class ConceptDetailsModel implements SectionModel {
    private String creationDate;
    private String therapeuticArea;
    private String linkedExperiment;
    private String projectCode;
    private List<String> conceptKeywords;
    private List<String> designers;
    private List<String> coauthors;

    public ConceptDetailsModel(String creationDate, String therapeuticArea,
                               String linkedExperiment, String projectCode,
                               List<String> conceptKeywords, List<String> designers, List<String> coauthors) {
        this.creationDate = creationDate;
        this.therapeuticArea = therapeuticArea;
        this.linkedExperiment = linkedExperiment;
        this.projectCode = projectCode;
        this.conceptKeywords = conceptKeywords;
        this.designers = designers;
        this.coauthors = coauthors;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getTherapeuticArea() {
        return therapeuticArea;
    }

    public String getLinkedExperiment() {
        return linkedExperiment;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public List<String> getConceptKeywords() {
        return conceptKeywords;
    }

    public List<String> getDesigners() {
        return designers;
    }

    public List<String> getCoauthors() {
        return coauthors;
    }
}
