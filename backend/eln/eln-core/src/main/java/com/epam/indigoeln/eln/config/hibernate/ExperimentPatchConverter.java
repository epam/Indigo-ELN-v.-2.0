package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.Converter;

@Converter
@Singleton
public class ExperimentPatchConverter extends AbstractJSONConverter<ExperimentModelPatch> {

    @Inject
    ExperimentPatchConverter(ObjectMapper objectMapper) {
        super(objectMapper, ExperimentModelPatch.class);
    }
}
