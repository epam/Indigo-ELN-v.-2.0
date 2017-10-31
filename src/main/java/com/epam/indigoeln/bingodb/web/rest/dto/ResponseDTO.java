package com.epam.indigoeln.bingodb.web.rest.dto;

import com.epam.indigoeln.bingodb.domain.BingoStructure;

import java.io.Serializable;
import java.util.List;

public class ResponseDTO implements Serializable {

    private List<BingoStructure> structures;

    public List<BingoStructure> getStructures() {
        return structures;
    }

    public void setStructures(List<BingoStructure> structures) {
        this.structures = structures;
    }
}
