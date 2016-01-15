package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Batch;

import java.io.Serializable;

public class BatchDTO implements Serializable, Comparable<BatchDTO> {

    private static final long serialVersionUID = -7878801074903420228L;

    private String id;
    private String batchNumber;

    public BatchDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    @Override
    public int compareTo(BatchDTO obj) {
        return obj.getBatchNumber() != null ? batchNumber.compareTo(obj.getBatchNumber()) : 1;
    }

    @Override
    public String toString() {
        return "BatchDTO{" +
                "id='" + id + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                '}';
    }
}
