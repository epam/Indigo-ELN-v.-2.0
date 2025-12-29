package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.Converter;

@Converter
@Singleton
public class ExperimentPatchConverter extends AbstractJSONConverter<ExperimentPatch> {

    @Inject
    ExperimentPatchConverter(ObjectMapper objectMapper) {
        super(objectMapper, ExperimentPatch.class);
    }
}
