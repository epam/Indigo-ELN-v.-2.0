package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Batch;

import java.io.Serializable;

public class BatchDTO implements Serializable {

    private static final long serialVersionUID = 13994253258215137L;

    private String id;
    private String batchNumber;
    private String virtualCompoundId;
    private String stereoIsomerCode;
    private String comments;
    private String structureComments;
    private Float  molecularWeight;
    private String formula;
    private String molfile;

    public BatchDTO() {
    }

    public BatchDTO(Batch batch) {
        this(batch.getId(),
             batch.getBatchNumber(),
             batch.getVirtualCompoundId(),
             batch.getStereoIsomerCode(),
             batch.getComments(),
             batch.getStructureComments());
    }

    public BatchDTO(String  id,
                    String  batchNumber,
                    String  virtualCompoundId,
                    String  stereoIsomerCode,
                    String  comments,
                    String  structureComments) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.virtualCompoundId = virtualCompoundId;
        this.stereoIsomerCode = stereoIsomerCode;
        this.comments = comments;
        this.structureComments = structureComments;
    }

    public String getId() {
        return id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public String getVirtualCompoundId() {
        return virtualCompoundId;
    }

    public String getStereoIsomerCode() {
        return stereoIsomerCode;
    }

    public String getComments() {
        return comments;
    }

    public String getStructureComments() {
        return structureComments;
    }

    public Float getMolecularWeight() {
        return molecularWeight;
    }

    public String getFormula() {
        return formula;
    }

    public String getMolfile() {
        return molfile;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public void setVirtualCompoundId(String virtualCompoundId) {
        this.virtualCompoundId = virtualCompoundId;
    }

    public void setStereoIsomerCode(String stereoIsomerCode) {
        this.stereoIsomerCode = stereoIsomerCode;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setStructureComments(String structureComments) {
        this.structureComments = structureComments;
    }

    public void setMolecularWeight(Float molecularWeight) {
        this.molecularWeight = molecularWeight;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setMolfile(String molfile) {
        this.molfile = molfile;
    }

    @Override
    public String toString() {
        return "BatchDTO{" +
                "id='" + id + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", virtualCompoundId='" + virtualCompoundId + '\'' +
                ", stereoIsomerCode='" + stereoIsomerCode + '\'' +
                ", comments='" + comments + '\'' +
                ", structureComments='" + structureComments + '\'' +
                ", molecularWeight=" + molecularWeight +
                ", formula='" + formula + '\'' +
                ", molfile='" + molfile + '\'' +
                '}';
    }
}
