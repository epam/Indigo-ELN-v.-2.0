package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;
import java.time.ZonedDateTime;
import java.util.List;

public class ConceptDetailsModel implements SectionModel {
    private ZonedDateTime creationDate;
    private String therapeuticArea;
    private List<String> linkedExperiment;
    private String projectCode;
    private String conceptKeywords;
    private List<String> designers;
    private List<String> coauthors;

    public ConceptDetailsModel(ZonedDateTime creationDate, String therapeuticArea,
                               List<String> linkedExperiment, String projectCode,
                               String conceptKeywords,  List<String> designers,  List<String> coauthors) {
        this.creationDate = creationDate;
        this.therapeuticArea = therapeuticArea;
        this.linkedExperiment = linkedExperiment;
        this.projectCode = projectCode;
        this.conceptKeywords = conceptKeywords;
        this.designers = designers;
        this.coauthors = coauthors;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public String getTherapeuticArea() {
        return therapeuticArea;
    }

    public  List<String> getLinkedExperiment() {
        return linkedExperiment;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public String getConceptKeywords() {
        return conceptKeywords;
    }

    public  List<String> getDesigners() {
        return designers;
    }

    public  List<String> getCoauthors() {
        return coauthors;
    }
}
