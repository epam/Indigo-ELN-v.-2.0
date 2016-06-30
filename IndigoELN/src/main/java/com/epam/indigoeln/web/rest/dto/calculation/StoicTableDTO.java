package com.epam.indigoeln.web.rest.dto.calculation;

import java.util.List;

public class StoicTableDTO {

    private List<BasicBatchModel> stoicBatches;
    private List<BasicBatchModel> intendedProducts;
    private List<BasicBatchModel> actualProducts;
    private int changedBatchRowNumber;
    private String changedField;
    private String lastUpdatedType;

    public StoicTableDTO() {
    }

    public StoicTableDTO(List<BasicBatchModel> stoicBatches, List<BasicBatchModel> intendedProducts, List<BasicBatchModel> actualProducts) {
        this.stoicBatches = stoicBatches;
        this.intendedProducts = intendedProducts;
        this.actualProducts = actualProducts;
    }

    public List<BasicBatchModel> getStoicBatches() {
        return stoicBatches;
    }

    public void setStoicBatches(List<BasicBatchModel> stoicBatches) {
        this.stoicBatches = stoicBatches;
    }

    public List<BasicBatchModel> getIntendedProducts() {
        return intendedProducts;
    }

    public void setIntendedProducts(List<BasicBatchModel> intendedProducts) {
        this.intendedProducts = intendedProducts;
    }

    public List<BasicBatchModel> getActualProducts() {
        return actualProducts;
    }

    public void setActualProducts(List<BasicBatchModel> actualProducts) {
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

    public String getLastUpdatedType() {
        return lastUpdatedType;
    }

    public void setLastUpdatedType(String lastUpdatedType) {
        this.lastUpdatedType = lastUpdatedType;
    }
}
