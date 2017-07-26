package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;

import java.time.ZonedDateTime;
import java.util.List;

public class BatchDetailsModel implements SectionModel {
    private String fullNbkBatch;
    private String registrationDate;
    private String structureComments;
    private String source;
    private String sourceDetail;
    private List<String> batchOwner;
    private String molWeight;
    private String formula;
    private String residualSolvent;
    private String solubility;
    private String precursors;
    private String externalSupplier;
    private String healthHazards;
    private String handlingPrecautions;
    private String storageInstructions;

    public BatchDetailsModel(String fullNbkBatch, String registrationDate, String structureComments,
                             String source, String sourceDetail, List<String> batchOwner, String molWeight, String formula,
                             String residualSolvent, String solubility, String precursors, String externalSupplier,
                             String healthHazards, String handlingPrecautions, String storageInstructions) {

        this.fullNbkBatch = fullNbkBatch;
        this.registrationDate = registrationDate;
        this.structureComments = structureComments;
        this.source = source;
        this.sourceDetail = sourceDetail;
        this.batchOwner = batchOwner;
        this.molWeight = molWeight;
        this.formula = formula;
        this.residualSolvent = residualSolvent;
        this.solubility = solubility;
        this.precursors = precursors;
        this.externalSupplier = externalSupplier;
        this.healthHazards = healthHazards;
        this.handlingPrecautions = handlingPrecautions;
        this.storageInstructions = storageInstructions;
    }

    public String getFullNbkBatch() {
        return fullNbkBatch;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public String getStructureComments() {
        return structureComments;
    }

    public String getSource() {
        return source;
    }

    public String getSourceDetail() {
        return sourceDetail;
    }

    public List<String> getBatchOwner() {
        return batchOwner;
    }

    public String getMolWeight() {
        return molWeight;
    }

    public String getFormula() {
        return formula;
    }

    public String getResidualSolvent() {
        return residualSolvent;
    }

    public String getSolubility() {
        return solubility;
    }

    public String getPrecursors() {
        return precursors;
    }

    public String getExternalSupplier() {
        return externalSupplier;
    }

    public String getHealthHazards() {
        return healthHazards;
    }

    public String getHandlingPrecautions() {
        return handlingPrecautions;
    }

    public String getStorageInstructions() {
        return storageInstructions;
    }
}
