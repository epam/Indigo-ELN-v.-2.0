package com.epam.indigo.bingodb.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BingoResult {

    private Boolean success;
    private String errorMessage;

    private Integer id;
    private String structure;

    private BingoResult(Boolean success) {
        this.success = success;
    }

    public static BingoResult success() {
        return new BingoResult(true);
    }

    public static BingoResult failure() {
        return new BingoResult(false);
    }

    public BingoResult withId(Integer id) {
        setId(id);
        return this;
    }

    public BingoResult withStructure(String structure) {
        setStructure(structure);
        return this;
    }

    public BingoResult withErrorMessage(String errorMessage) {
        setErrorMessage(errorMessage);
        return this;
    }

    public Boolean getSuccess() {
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
}
