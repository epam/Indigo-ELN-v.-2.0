package com.epam.indigoeln.eln.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Dictionary {

    THERAPEUTIC_AREA(false),
    PROJECT_CODE(false),
    PROJECT_KEYWORD(true),
    STEREOISOMER_CODE(false),
    TEST(false), // only for tests, not used in the application
    ;

    private final boolean usersCanAddNewItems;
}
