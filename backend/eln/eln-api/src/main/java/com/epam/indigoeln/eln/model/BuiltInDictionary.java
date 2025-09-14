package com.epam.indigoeln.eln.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BuiltInDictionary {

    THERAPEUTIC_AREA(UUID.fromString("84f97b23-7125-4847-bca2-399f351df9d7"), false),
    PROJECT_CODE(UUID.fromString("886d9a69-ae2f-4e19-ae0b-0d8f28f27e01"), false),
    PROJECT_KEYWORD(UUID.fromString("1cc9d41e-1e5e-4447-abd5-5067e9ba8210"), true),
    STEREOISOMER_CODE(UUID.fromString("8c61d750-c4da-431b-b5a1-b5ebb0a98811"), false),

    HEALTH_HAZARD(UUID.fromString("f9e02f6e-9408-4d25-a9eb-06cda2f78a4f"), true),
    HANDLING_PRECAUTIONS(UUID.fromString("7f07ee6f-f89d-4d3b-9e5b-14fe4302ae62"), true),
    STORAGE_INSTRUCTIONS(UUID.fromString("100c59b4-8c44-4884-943e-e4111357b862"), true),
    COMPOUND_PROTECTION(UUID.fromString("fdd3c5ad-7a70-4591-ae4c-206340547984"), true),
    SOLVENT(UUID.fromString("0e862734-b876-41cd-a8ad-0bf6b10af3d3"), true),
    EXTERNAL_SUPPLIER(UUID.fromString("8b7ad22e-b189-4418-87cd-cd655eec43e9"), true),
    SAMPLE_SOURCE(UUID.fromString("97a40780-2ab8-4de5-875a-afb4bc609910"), true),
    SAMPLE_SOURCE_DETAILS(UUID.fromString("a7b70b90-dc18-452c-a06b-887869c7c95d"), true),
    COMPONENT_STATE(UUID.fromString("e7bbdfa7-c495-4f8b-a384-e7f5ffd42dc5"), false),

    TEST(UUID.fromString("19c2ffe7-4ab8-4e4b-ba29-c291d1442b63"), false), // only for tests, not used in the application
    ;

    private final UUID id;
    private final boolean usersCanAddNewItems;
}
