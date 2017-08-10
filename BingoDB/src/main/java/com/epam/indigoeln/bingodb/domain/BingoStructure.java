package com.epam.indigoeln.bingodb.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
public class BingoStructure implements Serializable {

    private static final long serialVersionUID = 8965086228918128863L;

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
