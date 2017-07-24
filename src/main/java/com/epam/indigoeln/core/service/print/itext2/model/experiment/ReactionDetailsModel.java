package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;

import java.time.ZonedDateTime;

public class ReactionDetailsModel implements SectionModel {
    private ZonedDateTime creationDate;
    private String therapeuticArea;
    private String continuedFrom;
    private String continuedTo;
    private String projectCode;
    private String projectAlias;
    private String linkedExperiment;
    private String litretureReference;
    private String coauthors;

    public ReactionDetailsModel(ZonedDateTime creationDate, String therapeuticArea, String continuedFrom,
                                String continuedTo, String projectCode, String projectAlias,
                                String linkedExperiment, String litretureReference, String coauthors) {
        this.creationDate = creationDate;
        this.therapeuticArea = therapeuticArea;
        this.continuedFrom = continuedFrom;
        this.continuedTo = continuedTo;
        this.projectCode = projectCode;
        this.projectAlias = projectAlias;
        this.linkedExperiment = linkedExperiment;
        this.litretureReference = litretureReference;
        this.coauthors = coauthors;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public String getTherapeuticArea() {
        return therapeuticArea;
    }

    public String getContinuedFrom() {
        return continuedFrom;
    }

    public String getContinuedTo() {
        return continuedTo;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public String getProjectAlias() {
        return projectAlias;
    }

    public String getLinkedExperiment() {
        return linkedExperiment;
    }

    public String getLitretureReference() {
        return litretureReference;
    }

    public String getCoauthors() {
        return coauthors;
    }
}
