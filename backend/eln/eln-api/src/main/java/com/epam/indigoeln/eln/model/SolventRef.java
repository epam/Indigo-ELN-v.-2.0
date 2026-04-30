package com.epam.indigoeln.eln.model;

import java.util.UUID;

public class SolventRef extends DictionaryItemRef {

    public SolventRef(UUID id, String name, boolean active, boolean deleted, UUID dictionaryID) {
        super(id, name, active, deleted, dictionaryID);
    }
}
