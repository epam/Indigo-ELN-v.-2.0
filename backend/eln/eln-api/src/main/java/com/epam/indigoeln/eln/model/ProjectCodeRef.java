package com.epam.indigoeln.eln.model;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectCodeRef extends DictionaryItemRef {
    public ProjectCodeRef(UUID id, String name) {
        super(id, name);
    }
}
