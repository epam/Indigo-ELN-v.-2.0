package com.epam.indigoeln.eln.model;

import java.util.UUID;

public class CompoundProtectionRef extends DictionaryItemRef {

    public CompoundProtectionRef(UUID id, String name, boolean active, boolean deleted, UUID dictionaryID) {
        super(id, name, active, deleted, dictionaryID);
    }
}
