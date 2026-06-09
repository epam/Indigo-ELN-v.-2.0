package com.epam.indigoeln.eln.model;

import java.util.UUID;

public class TherapeuticAreaRef extends DictionaryItemRef {

    public TherapeuticAreaRef(UUID id, String name, boolean active, boolean deleted, UUID dictionaryID) {
        super(id, name, active, deleted, dictionaryID);
    }
}
