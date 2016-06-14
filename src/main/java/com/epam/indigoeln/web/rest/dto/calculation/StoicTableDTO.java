package com.epam.indigoeln.web.rest.dto.calculation;

import java.util.List;

public class StoicTableDTO {

    private List<StoicBatchDTO> stoicBatches;
    private List<StoicBatchDTO> intendedProducts;
    private List<StoicBatchDTO> actualProducts;
    private StoicBatchDTO changedBatch;
    private String changedField;

    public StoicTableDTO() {
    }

    public StoicTableDTO(List<StoicBatchDTO> stoicBatches, List<StoicBatchDTO> intendedProducts, List<StoicBatchDTO> actualProducts) {
        this.stoicBatches = stoicBatches;
        this.intendedProducts = intendedProducts;
        this.actualProducts = actualProducts;
    }

    public List<StoicBatchDTO> getStoicBatches() {
        return stoicBatches;
    }

    public void setStoicBatches(List<StoicBatchDTO> stoicBatches) {
        this.stoicBatches = stoicBatches;
    }

    public List<StoicBatchDTO> getIntendedProducts() {
        return intendedProducts;
    }

    public void setIntendedProducts(List<StoicBatchDTO> intendedProducts) {
        this.intendedProducts = intendedProducts;
    }

    public List<StoicBatchDTO> getActualProducts() {
        return actualProducts;
    }

    public void setActualProducts(List<StoicBatchDTO> actualProducts) {
        this.actualProducts = actualProducts;
    }

    public StoicBatchDTO getChangedBatch() {
        return changedBatch;
    }

    public void setChangedBatch(StoicBatchDTO changedBatch) {
        this.changedBatch = changedBatch;
    }

    public String getChangedField() {
        return changedField;
    }
}
