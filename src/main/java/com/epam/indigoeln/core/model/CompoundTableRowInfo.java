package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.service.search.impl.pubchem.dto.Structure;

public class CompoundTableRowInfo {

    private String chemicalName;

    private String comments;

    private String compoundId;

    private String saltEq;

    private Structure structure;

    private String formula;

    private String fullNbkBatch;

    private String hazardComments;

    private String molecularWeight;

    private String saltCode;

    private String conversationalBatchNumber;

    private String casNumber;

    private String stereoisomerCode;

    public String getChemicalName() {
        return chemicalName;
    }

    public void setChemicalName(String chemicalName) {
        this.chemicalName = chemicalName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCompoundId() {
        return compoundId;
    }

    public void setCompoundId(String compoundId) {
        this.compoundId = compoundId;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public String getSaltEq() {
        return saltEq;
    }

    public void setSaltEq(String saltEq) {
        this.saltEq = saltEq;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getFullNbkBatch() {
        return fullNbkBatch;
    }

    public void setFullNbkBatch(String fullNbkBatch) {
        this.fullNbkBatch = fullNbkBatch;
    }

    public String getHazardComments() {
        return hazardComments;
    }

    public void setHazardComments(String hazardComments) {
        this.hazardComments = hazardComments;
    }

    public String getMolecularWeight() {
        return molecularWeight;
    }

    public void setMolecularWeight(String molecularWeight) {
        this.molecularWeight = molecularWeight;
    }


    public String getSaltCode() {
        return saltCode;
    }

    public void setSaltCode(String saltCode) {
        this.saltCode = saltCode;
    }

    public String getConversationalBatchNumber() {
        return conversationalBatchNumber;
    }

    public void setConversationalBatchNumber(String conversationalBatchNumber) {
        this.conversationalBatchNumber = conversationalBatchNumber;
    }

    public String getCasNumber() {
        return casNumber;
    }

    public void setCasNumber(String casNumber) {
        this.casNumber = casNumber;
    }

    public String getStereoisomerCode() {
        return stereoisomerCode;
    }

    public void setStereoisomerCode(String stereoisomerCode) {
        this.stereoisomerCode = stereoisomerCode;
    }
}
