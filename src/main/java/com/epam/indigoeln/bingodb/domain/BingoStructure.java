package com.epam.indigoeln.bingodb.domain;

import java.io.Serializable;

public class BingoStructure implements Serializable {

    private String id;
    private String structure;

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
