package com.epam.indigoeln.web.rest.dto;

import java.io.Serializable;

public class BatchDTO implements Serializable {

    private static final long serialVersionUID = 13994253258215137L;

    private String id;
    private String batchNumber;

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
    public String toString() {
        return "BatchDTO{" +
                "id='" + id + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                '}';
    }
}
