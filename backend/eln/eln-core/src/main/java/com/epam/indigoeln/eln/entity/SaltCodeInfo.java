package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.reaction.model.SaltCodeRef;
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

    public SaltCodeRef toRef() {
        return new SaltCodeRef(id, code, name);
    }
}
