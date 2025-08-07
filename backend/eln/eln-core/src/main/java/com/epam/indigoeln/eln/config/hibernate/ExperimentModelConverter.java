package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

@Converter
@ApplicationScoped
public class ExperimentModelConverter implements AttributeConverter<ExperimentModel, String> {

    @Inject
    ExperimentModelService experimentModelService;

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable ExperimentModel attribute) {
        return attribute != null ? experimentModelService.serializeModel(attribute) : null;
    }

    @Override
    @Nullable
    public ExperimentModel convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null ? experimentModelService.deserializeModel(dbData) : null;
    }
}
