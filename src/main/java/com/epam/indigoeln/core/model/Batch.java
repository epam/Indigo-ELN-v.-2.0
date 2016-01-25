package com.epam.indigoeln.core.model;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * Batch model object
 */
public class Batch implements Serializable {

    private static final long serialVersionUID = -4780589192255949497L;

    @NotNull
    @Pattern(regexp = "^[a-z0-9]*")
    private String id;

    @NotBlank
    private String batchNumber;

    private String virtualCompoundId;

    //TODO: should be replaced to Dictionary Item after Dictionaries implementation
    private String stereoIsomerCode;

    private String comments;

    private String structureComments;

    private Integer bingoDbId;


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

    public Integer getBingoDbId() {
        return bingoDbId;
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

    public void setBingoDbId(Integer bingoDbId) {
        this.bingoDbId = bingoDbId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Batch batch = (Batch) o;

        if (id != null ? !id.equals(batch.id) : batch.id != null) return false;
        if (batchNumber != null ? !batchNumber.equals(batch.batchNumber) : batch.batchNumber != null) return false;
        if (virtualCompoundId != null ? !virtualCompoundId.equals(batch.virtualCompoundId) : batch.virtualCompoundId != null)
            return false;
        if (stereoIsomerCode != null ? !stereoIsomerCode.equals(batch.stereoIsomerCode) : batch.stereoIsomerCode != null)
            return false;
        if (comments != null ? !comments.equals(batch.comments) : batch.comments != null) return false;
        if (structureComments != null ? !structureComments.equals(batch.structureComments) : batch.structureComments != null)
            return false;
        return bingoDbId != null ? bingoDbId.equals(batch.bingoDbId) : batch.bingoDbId == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (batchNumber != null ? batchNumber.hashCode() : 0);
        result = 31 * result + (virtualCompoundId != null ? virtualCompoundId.hashCode() : 0);
        result = 31 * result + (stereoIsomerCode != null ? stereoIsomerCode.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        result = 31 * result + (structureComments != null ? structureComments.hashCode() : 0);
        result = 31 * result + (bingoDbId != null ? bingoDbId.hashCode() : 0);
        return result;
    }
}
