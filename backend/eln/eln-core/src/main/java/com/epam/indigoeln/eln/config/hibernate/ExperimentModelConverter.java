package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

@Converter
@ApplicationScoped
public class ExperimentModelConverter implements AttributeConverter<ExperimentModel, String> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    @Nullable
    @SneakyThrows
    public String convertToDatabaseColumn(@Nullable ExperimentModel attribute) {
        return attribute != null ? objectMapper.writeValueAsString(attribute) : null;
    }

    @Override
    @Nullable
    @SneakyThrows
    public ExperimentModel convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null ? objectMapper.readValue(dbData, ExperimentModel.class) : null;
    }
}
