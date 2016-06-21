package com.epam.indigoeln.web.rest.dto.calculation;

import java.util.List;

public class StoicTableDTO {

    private List<StoicBatchDTO> stoicBatches;
    private List<StoicBatchDTO> intendedProducts;
    private List<StoicBatchDTO> actualProducts;
    private int changedBatchRowNumber;
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

    public int getChangedBatchRowNumber() {
        return changedBatchRowNumber;
    }

    public void setChangedBatchRowNumber(int changedBatchRowNumber) {
        this.changedBatchRowNumber = changedBatchRowNumber;
    }

    public String getChangedField() {
        return changedField;
    }

    public void setChangedField(String changedField) {
        this.changedField = changedField;
    }
}
