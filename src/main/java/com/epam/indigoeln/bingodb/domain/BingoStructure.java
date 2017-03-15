package com.epam.indigoeln.bingodb.domain;

public class BingoStructure {

    private String id;
    private String structure;

    public BingoStructure() {
    }

    public BingoStructure(String id, String structure) {
        this.id = id;
        this.structure = structure;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }
}
