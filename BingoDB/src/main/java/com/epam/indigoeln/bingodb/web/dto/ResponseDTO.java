package com.epam.indigoeln.bingodb.web.dto;

import com.epam.indigoeln.bingodb.domain.BingoStructure;

import java.util.List;

public class ResponseDTO {

    private String message;
    private List<BingoStructure> structures;

    public ResponseDTO() {
    }

    public ResponseDTO(String message) {
        this.message = message;
    }

    public ResponseDTO(List<BingoStructure> structures) {
        this.structures = structures;
    }

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
