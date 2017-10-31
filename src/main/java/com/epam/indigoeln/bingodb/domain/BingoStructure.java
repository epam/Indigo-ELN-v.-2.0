package com.epam.indigoeln.bingodb.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class BingoStructure implements Serializable {

    private String id;
    private String structure;

    public BingoStructure() {
        // Empty constructor for JSON deserialize
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
