package com.epam.indigoeln.bingodb.web.rest.dto;

import com.epam.indigoeln.bingodb.domain.BingoStructure;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode
public class ResponseDTO implements Serializable {

    private List<BingoStructure> structures;

    public ResponseDTO() {
        // Empty constructor for JSON deserialize
    }

    public ResponseDTO(List<BingoStructure> structures) {
        this.structures = structures;
    }

    public List<BingoStructure> getStructures() {
        return structures;
    }

    public void setStructures(List<BingoStructure> structures) {
        this.structures = structures;
    }
}
