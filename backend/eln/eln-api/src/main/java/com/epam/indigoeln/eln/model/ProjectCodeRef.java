package com.epam.indigoeln.eln.model;

import java.util.UUID;

public class ProjectCodeRef extends DictionaryItemRef {

    public ProjectCodeRef(UUID id, String name, boolean active, boolean deleted, UUID dictionaryID) {
        super(id, name, active, deleted, dictionaryID);
    }
}
