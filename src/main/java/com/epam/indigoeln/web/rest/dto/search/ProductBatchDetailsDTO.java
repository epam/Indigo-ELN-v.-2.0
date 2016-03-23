package com.epam.indigoeln.web.rest.dto.search;

import java.util.Map;

public class ProductBatchDetailsDTO {

    private String notebookBatchNumber;
    private Map details;

    public ProductBatchDetailsDTO() {
        super();
    }

    public ProductBatchDetailsDTO(String notebookBatchNumber, Map details) {
        this.notebookBatchNumber = notebookBatchNumber;
        this.details = details;
    }

    public String getNotebookBatchNumber() {
        return notebookBatchNumber;
    }

    public Map getDetails() {
        return details;
    }

    public void setNotebookBatchNumber(String notebookBatchNumber) {
        this.notebookBatchNumber = notebookBatchNumber;
    }

    public void setDetails(Map details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "ProductBatchDetailsDTO{" +
                "notebookBatchNumber='" + notebookBatchNumber + '\'' +
                ", details=" + details +
                '}';
    }
}
