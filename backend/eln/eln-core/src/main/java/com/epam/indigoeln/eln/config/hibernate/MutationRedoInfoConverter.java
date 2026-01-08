package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.Converter;

@Converter
@Singleton
public class MutationRedoInfoConverter extends AbstractJSONConverter<MutationRedoInfo> {

    @Inject
    MutationRedoInfoConverter(ObjectMapper objectMapper) {
        super(objectMapper, MutationRedoInfo.class);
    }
}
