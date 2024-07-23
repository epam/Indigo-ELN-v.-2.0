package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.chemistry.domain.Structure;

public class CompoundTableRowInfo {

    private String chemicalName;

    private String comments;

    private String compoundId;

    private SaltEquivalent saltEq;

    private Structure structure;

    private String formula;

    private String fullNbkBatch;

    private String hazardComments;

    private MolecularWeight molWeight;

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

    public SaltEquivalent getSaltEq() {
        return saltEq;
    }

    public void setSaltEq(SaltEquivalent saltEq) {
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

    public MolecularWeight getMolWeight() {
        return molWeight;
    }

    public void setMolWeight(MolecularWeight molecularWeight) {
        this.molWeight = molecularWeight;
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
