package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.model.TemplateComponent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Converter
@ApplicationScoped
public class TemplateComponentListConverter implements AttributeConverter<List<TemplateComponent>, String> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    @Nullable
    @SneakyThrows
    public String convertToDatabaseColumn(@Nullable List<TemplateComponent> attribute) {
        return attribute != null ? objectMapper.writeValueAsString(attribute) : null;
    }

    @Override
    @Nullable
    @SneakyThrows
    public List<TemplateComponent> convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null ? objectMapper.readValue(dbData, new TypeReference<>() {
        }) : null;
    }
}
