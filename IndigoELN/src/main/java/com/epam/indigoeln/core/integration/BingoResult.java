package com.epam.indigoeln.core.integration;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BingoResult implements Serializable {

    private static final long serialVersionUID = 4234869853216112920L;

    private Boolean success;
    private String  errorMessage;
    private Integer id;
    private String structure;
    private List<String> searchResult;

    public BingoResult() {
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public List<String> getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(List<String> searchResult) {
        this.searchResult = searchResult;
    }
}
