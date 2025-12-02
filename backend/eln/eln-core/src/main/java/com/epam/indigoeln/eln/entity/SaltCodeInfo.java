package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import lombok.Value;

import java.util.UUID;

@Value
public class SaltCodeInfo {

    UUID id;
    String code;
    String name;
    String formula;
    int charge;
    double molWeight;

    public DictionaryItemRef toRef() {
        return new DictionaryItemRef(id, name);
    }
}
