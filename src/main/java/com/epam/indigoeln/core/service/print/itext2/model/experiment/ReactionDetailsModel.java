package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;
import java.time.ZonedDateTime;
import java.util.List;

public class ReactionDetailsModel implements SectionModel {
    private ZonedDateTime creationDate;
    private String therapeuticArea;
    private List<String> continuedFrom;
    private List<String> continuedTo;
    private String projectCode;
    private String projectAlias;
    private List<String> linkedExperiment;
    private String literatureReference;
    private List<String> coauthors;

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public String getTherapeuticArea() {
        return therapeuticArea;
    }

    public List<String> getContinuedFrom() {
        return continuedFrom;
    }

    public List<String> getContinuedTo() {
        return continuedTo;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public String getProjectAlias() {
        return projectAlias;
    }

    public List<String> getLinkedExperiment() {
        return linkedExperiment;
    }

    public String getLiteratureReference() {
        return literatureReference;
    }

    public List<String> getCoauthors() {
        return coauthors;
    }

    public ReactionDetailsModel setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public ReactionDetailsModel setTherapeuticArea(String therapeuticArea) {
        this.therapeuticArea = therapeuticArea;
        return this;
    }

    public ReactionDetailsModel setContinuedFrom(List<String> continuedFrom) {
        this.continuedFrom = continuedFrom;
        return this;
    }

    public ReactionDetailsModel setContinuedTo(List<String> continuedTo) {
        this.continuedTo = continuedTo;
        return this;
    }

    public ReactionDetailsModel setProjectCode(String projectCode) {
        this.projectCode = projectCode;
        return this;
    }

    public ReactionDetailsModel setProjectAlias(String projectAlias) {
        this.projectAlias = projectAlias;
        return this;
    }

    public ReactionDetailsModel setLinkedExperiment(List<String> linkedExperiment) {
        this.linkedExperiment = linkedExperiment;
        return this;
    }

    public ReactionDetailsModel setLiteratureReference(String literatureReference) {
        this.literatureReference = literatureReference;
        return this;
    }

    public ReactionDetailsModel setCoauthors(List<String> coauthors) {
        this.coauthors = coauthors;
        return this;
    }
}
