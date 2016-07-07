package com.epam.indigoeln.core.model;

import java.io.Serializable;

public class Compound implements Serializable {

    private static final long serialVersionUID = 8575074604535672749L;

    private String chemicalName;

    private String compoundNo;

    private String conversationalBatchNo;

    private String batchNo;

    private String casNo;

    private String structure;

    private String formula;

    private String stereoisomerCode;

    private String saltCode;

    private double saltEquivs;

    private String comment;

    private String hazardComment;

    private String storageComment;

    private String registrationStatus;

    public String getChemicalName() {
        return chemicalName;
    }

    public void setChemicalName(String chemicalName) {
        this.chemicalName = chemicalName;
    }

    public String getCompoundNo() {
        return compoundNo;
    }

    public void setCompoundNo(String compoundNo) {
        this.compoundNo = compoundNo;
    }

    public String getConversationalBatchNo() {
        return conversationalBatchNo;
    }

    public void setConversationalBatchNo(String conversationalBatchNo) {
        this.conversationalBatchNo = conversationalBatchNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getCasNo() {
        return casNo;
    }

    public void setCasNo(String casNo) {
        this.casNo = casNo;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getStereoisomerCode() {
        return stereoisomerCode;
    }

    public void setStereoisomerCode(String stereoisomerCode) {
        this.stereoisomerCode = stereoisomerCode;
    }

    public String getSaltCode() {
        return saltCode;
    }

    public void setSaltCode(String saltCode) {
        this.saltCode = saltCode;
    }

    public double getSaltEquivs() {
        return saltEquivs;
    }

    public void setSaltEquivs(double saltEquivs) {
        this.saltEquivs = saltEquivs;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getHazardComment() {
        return hazardComment;
    }

    public void setHazardComment(String hazardComment) {
        this.hazardComment = hazardComment;
    }

    public String getStorageComment() {
        return storageComment;
    }

    public void setStorageComment(String storageComment) {
        this.storageComment = storageComment;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    @Override
    public String toString() {
        return "Compound{" +
                "compoundNo='" + compoundNo + '\'' +
                ", conversationalBatchNo='" + conversationalBatchNo + '\'' +
                ", batchNo='" + batchNo + '\'' +
                '}';
    }
}
