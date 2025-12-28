package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.Converter;

@Converter
@Singleton
public class MutationConverter extends AbstractJSONConverter<Mutation> {

    @Inject
    MutationConverter(ObjectMapper objectMapper) {
        super(objectMapper, Mutation.class);
    }
}
