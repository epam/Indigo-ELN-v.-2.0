package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SaltCodeRef extends DictionaryItemRef {

    @JsonIgnore
    private final String code;

    @JsonIgnore
    private final String formula;

    @JsonIgnore
    private final int charge;

    @JsonIgnore
    private final double molWeight;

    public SaltCodeRef(UUID id, String name, boolean active, boolean deleted, UUID dictionaryID, String code, String formula, int charge, double molWeight) {
        super(id, name, active, deleted, dictionaryID);
        this.code = code;
        this.formula = formula;
        this.charge = charge;
        this.molWeight = molWeight;
    }
}
