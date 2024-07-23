/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;

import java.util.Date;
import java.util.List;

/**
 * Implementation of SectionModel interface for batch details.
 */
public class BatchDetailsModel implements SectionModel {
    private String fullNbkBatch;
    private Date registrationDate;
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

    public String getFullNbkBatch() {
        return fullNbkBatch;
    }

    public Date getRegistrationDate() {
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

    public BatchDetailsModel setFullNbkBatch(String fullNbkBatch) {
        this.fullNbkBatch = fullNbkBatch;
        return this;
    }

    public BatchDetailsModel setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
        return this;
    }

    public BatchDetailsModel setStructureComments(String structureComments) {
        this.structureComments = structureComments;
        return this;
    }

    public BatchDetailsModel setSource(String source) {
        this.source = source;
        return this;
    }

    public BatchDetailsModel setSourceDetail(String sourceDetail) {
        this.sourceDetail = sourceDetail;
        return this;
    }

    public BatchDetailsModel setBatchOwner(List<String> batchOwner) {
        this.batchOwner = batchOwner;
        return this;
    }

    public BatchDetailsModel setMolWeight(String molWeight) {
        this.molWeight = molWeight;
        return this;
    }

    public BatchDetailsModel setFormula(String formula) {
        this.formula = formula;
        return this;
    }

    public BatchDetailsModel setResidualSolvent(String residualSolvent) {
        this.residualSolvent = residualSolvent;
        return this;
    }

    public BatchDetailsModel setSolubility(String solubility) {
        this.solubility = solubility;
        return this;
    }

    public BatchDetailsModel setPrecursors(String precursors) {
        this.precursors = precursors;
        return this;
    }

    public BatchDetailsModel setExternalSupplier(String externalSupplier) {
        this.externalSupplier = externalSupplier;
        return this;
    }

    public BatchDetailsModel setHealthHazards(String healthHazards) {
        this.healthHazards = healthHazards;
        return this;
    }

    public BatchDetailsModel setHandlingPrecautions(String handlingPrecautions) {
        this.handlingPrecautions = handlingPrecautions;
        return this;
    }

    public BatchDetailsModel setStorageInstructions(String storageInstructions) {
        this.storageInstructions = storageInstructions;
        return this;
    }
}
