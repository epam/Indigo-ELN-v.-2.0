package com.epam.indigoeln.core.service.bingo.dto;

import java.util.List;

public class BingoResponse {

    private String message;
    private List<BingoStructure> structures;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BingoStructure> getStructures() {
        return structures;
    }

    public void setStructures(List<BingoStructure> structures) {
        this.structures = structures;
    }
}
