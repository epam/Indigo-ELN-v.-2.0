package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ELNJacksonCustomizer implements ObjectMapperCustomizer {

    @Inject
    DictionaryService dictionaryService;

    @Override
    public void customize(ObjectMapper objectMapper) {
        SimpleModule module = new SimpleModule();
        DictionaryItemRefDeserializer deserializer = new DictionaryItemRefDeserializer(dictionaryService);
        module.addDeserializer(DictionaryItemRef.class, deserializer);
        for (BuiltInDictionary builtInDictionary : BuiltInDictionary.values()) {
            //noinspection unchecked,rawtypes
            module.addDeserializer(builtInDictionary.getRefClass(), (JsonDeserializer) deserializer);
        }
        objectMapper.registerModule(module);
    }
}
