package com.epam.indigoeln.eln.model;

import java.util.UUID;

public class ProjectKeywordRef extends DictionaryItemRef {

    public ProjectKeywordRef(UUID id, String name, boolean active, boolean deleted, UUID dictionaryID) {
        super(id, name, active, deleted, dictionaryID);
    }
}
