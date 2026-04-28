package com.epam.indigoeln.eln.model;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolventRef extends DictionaryItemRef{
    public SolventRef(UUID id, String name) {
        super(id, name);
    }
}
