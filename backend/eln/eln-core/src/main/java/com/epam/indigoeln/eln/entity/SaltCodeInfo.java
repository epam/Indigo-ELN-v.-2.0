package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class SaltCodeInfo extends DictionaryItemRef {

    String code;
    String formula;
    int charge;
    double molWeight;

    public SaltCodeInfo(UUID id, String name, String code, String formula, int charge, double molWeight) {
        super(id, name);
        this.code = code;
        this.formula = formula;
        this.charge = charge;
        this.molWeight = molWeight;
    }
}
