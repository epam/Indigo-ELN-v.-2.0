package com.epam.indigoeln.core.model;

import java.io.Serializable;

/**
 * Batch model object
 */
public class Batch implements Serializable {

    private static final long serialVersionUID = -4780589192255949497L;

    private String id;
    private String batchNumber;

    public String getId() {
        return id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Batch batch = (Batch) o;

        if (id != null ? !id.equals(batch.id) : batch.id != null) return false;
        return batchNumber != null ? batchNumber.equals(batch.batchNumber) : batch.batchNumber == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (batchNumber != null ? batchNumber.hashCode() : 0);
        return result;
    }
}
