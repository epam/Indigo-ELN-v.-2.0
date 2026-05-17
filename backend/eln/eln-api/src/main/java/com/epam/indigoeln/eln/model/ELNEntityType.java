package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.common.model.EntityType;

public enum ELNEntityType implements EntityType {

    SYSTEM,

    PROJECT,
    NOTEBOOK,
    EXPERIMENT,
    TEMPLATE,

    ATTACHMENT,

    USER,
    ROLE,

    COMPOUND,
    SAMPLE,

    DICTIONARY,
    DICTIONARY_ITEM,
}
