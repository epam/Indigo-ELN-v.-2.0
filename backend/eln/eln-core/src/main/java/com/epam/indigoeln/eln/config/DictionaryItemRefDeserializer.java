package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.IOException;
import java.util.UUID;

public class DictionaryItemRefDeserializer extends StdDeserializer<DictionaryItemRef> {

    private final DictionaryService dictionaryService;

    public DictionaryItemRefDeserializer(DictionaryService dictionaryService) {
        super(DictionaryItemRef.class);
        this.dictionaryService = dictionaryService;
    }

    @Override
    public DictionaryItemRef deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectCodec codec = p.getCodec();
        Representation representation = codec.readValue(p, Representation.class);
        return dictionaryService.get(representation.id());
    }

    @RegisterForReflection
    record Representation(
            UUID id,
            String name
    ) {}
}
