package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Implementation of SectionModel interface for reaction details
 */
public class ReactionDetailsModel implements SectionModel {
    private ZonedDateTime creationDate;
    private String therapeuticArea;
    private String continuedFrom;
    private String continuedTo;
    private String projectCode;
    private String projectAlias;
    private List<String> linkedExperiment;
    private String literatureReference;
    private List<String> coAuthors;

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

    public List<String> getLinkedExperiment() {
        return linkedExperiment;
    }

    public String getLiteratureReference() {
        return literatureReference;
    }

    public List<String> getCoAuthors() {
        return coAuthors;
    }

    public ReactionDetailsModel setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public ReactionDetailsModel setTherapeuticArea(String therapeuticArea) {
        this.therapeuticArea = therapeuticArea;
        return this;
    }

    public ReactionDetailsModel setContinuedFrom(String continuedFrom) {
        this.continuedFrom = continuedFrom;
        return this;
    }

    public ReactionDetailsModel setContinuedTo(String continuedTo) {
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

    public ReactionDetailsModel setCoAuthors(List<String> coAuthors) {
        this.coAuthors = coAuthors;
        return this;
    }

    public ReactionDetailsModel(ZonedDateTime creationDate, String therapeuticArea, String continuedFrom, String continuedTo, String projectCode, String projectAlias, List<String> linkedExperiment, String literatureReference, List<String> coAuthors) {
        this.creationDate = creationDate;
        this.therapeuticArea = therapeuticArea;
        this.continuedFrom = continuedFrom;
        this.continuedTo = continuedTo;
        this.projectCode = projectCode;
        this.projectAlias = projectAlias;
        this.linkedExperiment = linkedExperiment;
        this.literatureReference = literatureReference;
        this.coAuthors = coAuthors;
    }
}
