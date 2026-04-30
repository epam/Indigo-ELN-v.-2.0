package com.epam.indigoeln.eln.model;

import java.util.UUID;

public class ComponentStateRef extends DictionaryItemRef {

    public ComponentStateRef(UUID id, String name, boolean active, boolean deleted, UUID dictionaryID) {
        super(id, name, active, deleted, dictionaryID);
    }
}
