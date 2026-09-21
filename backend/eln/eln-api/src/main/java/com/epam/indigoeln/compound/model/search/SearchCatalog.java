package com.epam.indigoeln.compound.model.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SearchCatalog {

    ELN(0),
    PUBCHEM(1000),
    MY_MATERIALS(1),
    SAMPLE_REGISTRATION_SERVICE(2);

    @Getter
    private final int priority;
}
