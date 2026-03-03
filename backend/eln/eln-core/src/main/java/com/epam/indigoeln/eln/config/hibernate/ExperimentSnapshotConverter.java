package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.Converter;

@Converter
@Singleton
public class ExperimentSnapshotConverter extends AbstractJSONConverter<ExperimentSnapshot> {

    @Inject
    ExperimentSnapshotConverter(ObjectMapper objectMapper) {
        super(objectMapper, ExperimentSnapshot.class);
    }
}
