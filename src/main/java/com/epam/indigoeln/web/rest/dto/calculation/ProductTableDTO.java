package com.epam.indigoeln.web.rest.dto.calculation;

public class ProductTableDTO {

    private BasicBatchModel productBatch;
    private String changedField;

    public ProductTableDTO() {
    }

    public ProductTableDTO(BasicBatchModel productBatch, String changedField) {
        this.productBatch = productBatch;
        this.changedField = changedField;
    }

    public BasicBatchModel getProductBatch() {
        return productBatch;
    }

    public void setProductBatch(BasicBatchModel productBatch) {
        this.productBatch = productBatch;
    }

    public String getChangedField() {
        return changedField;
    }

    public void setChangedField(String changedField) {
        this.changedField = changedField;
    }
}
