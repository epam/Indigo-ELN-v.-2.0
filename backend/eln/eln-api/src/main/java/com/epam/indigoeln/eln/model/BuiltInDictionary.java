package com.epam.indigoeln.eln.model;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BuiltInDictionary {

    THERAPEUTIC_AREA(UUID.fromString("84f97b23-7125-4847-bca2-399f351df9d7"), TherapeuticAreaRef.class, false),
    PROJECT_CODE(UUID.fromString("886d9a69-ae2f-4e19-ae0b-0d8f28f27e01"), ProjectCodeRef.class, false),
    PROJECT_KEYWORD(UUID.fromString("1cc9d41e-1e5e-4447-abd5-5067e9ba8210"), ProjectKeywordRef.class, true),
    STEREOISOMER_CODE(UUID.fromString("8c61d750-c4da-431b-b5a1-b5ebb0a98811"), StereoisomerCodeRef.class, false),

    HEALTH_HAZARD(UUID.fromString("f9e02f6e-9408-4d25-a9eb-06cda2f78a4f"), HealthHazardRef.class, true),
    HANDLING_PRECAUTIONS(UUID.fromString("7f07ee6f-f89d-4d3b-9e5b-14fe4302ae62"), HandlingPrecautionsRef.class, true),
    STORAGE_INSTRUCTIONS(UUID.fromString("100c59b4-8c44-4884-943e-e4111357b862"), StorageInstructionsRef.class, true),
    COMPOUND_PROTECTION(UUID.fromString("fdd3c5ad-7a70-4591-ae4c-206340547984"), CompoundProtectionRef.class, true),
    SOLVENT(UUID.fromString("0e862734-b876-41cd-a8ad-0bf6b10af3d3"), SolventRef.class, true),
    EXTERNAL_SUPPLIER(UUID.fromString("8b7ad22e-b189-4418-87cd-cd655eec43e9"), ExternalSupplierRef.class, true),
    SAMPLE_SOURCE(UUID.fromString("97a40780-2ab8-4de5-875a-afb4bc609910"), SampleSourceRef.class, true),
    SAMPLE_SOURCE_DETAILS(UUID.fromString("a7b70b90-dc18-452c-a06b-887869c7c95d"), SampleSourceDetailsRef.class, true),
    COMPONENT_STATE(UUID.fromString("e7bbdfa7-c495-4f8b-a384-e7f5ffd42dc5"), ComponentStateRef.class, false),
    SALT_CODE(UUID.fromString("978ac7bf-4474-4197-a28a-072cd65a70cb"), SaltCodeRef.class, false),
    ;

    private static final Map<String, BuiltInDictionary> BY_NAME = StreamEx.of(values())
            .toMap(Enum::name, Function.identity());

    private static final Map<UUID, BuiltInDictionary> BY_ID = StreamEx.of(values())
            .toMap(BuiltInDictionary::getId, Function.identity());

    private final UUID id;
    private final Class<? extends DictionaryItemRef> refClass;
    private final boolean usersCanAddNewItems;

    @Nullable
    public static BuiltInDictionary lookup(String name) {
        return BY_NAME.get(name);
    }

    @Nullable
    public static BuiltInDictionary lookup(UUID id) {
        return BY_ID.get(id);
    }
}
