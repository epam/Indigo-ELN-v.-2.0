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

    public ReactionDetailsModel(ZonedDateTime creationDate, String therapeuticArea, List<String> continuedFrom,
                                List<String> continuedTo, String projectCode, String projectAlias,
                                List<String> linkedExperiment, String literatureReference, List<String> coauthors) {
        this.creationDate = creationDate;
        this.therapeuticArea = therapeuticArea;
        this.continuedFrom = continuedFrom;
        this.continuedTo = continuedTo;
        this.projectCode = projectCode;
        this.projectAlias = projectAlias;
        this.linkedExperiment = linkedExperiment;
        this.literatureReference = literatureReference;
        this.coauthors = coauthors;
    }

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
}
