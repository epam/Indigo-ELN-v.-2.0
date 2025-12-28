package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.Converter;

@Converter
@Singleton
public class ExperimentModelConverter extends AbstractJSONConverter<ExperimentModel> {

    @Inject
    ExperimentModelConverter(ObjectMapper objectMapper) {
        super(objectMapper, ExperimentModel.class);
    }
}
