package com.epam.indigoeln.eln.model;

import java.util.UUID;

public class HealthHazardRef extends DictionaryItemRef {

    public HealthHazardRef(UUID id, String name, boolean active, boolean deleted, UUID dictionaryID) {
        super(id, name, active, deleted, dictionaryID);
    }
}
