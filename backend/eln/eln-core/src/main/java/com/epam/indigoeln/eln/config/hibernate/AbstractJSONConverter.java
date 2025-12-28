package com.epam.indigoeln.eln.config.hibernate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.persistence.AttributeConverter;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

abstract class AbstractJSONConverter<T> implements AttributeConverter<T, String> {

    private final ObjectReader reader;
    private final ObjectWriter writer;

    protected AbstractJSONConverter(ObjectMapper objectMapper, Class<T> klass) {
        reader = objectMapper.readerFor(klass);
        writer = objectMapper.writerFor(klass);
    }

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable T attribute) {
        try {
            return attribute != null ? writer.writeValueAsString(attribute) : null;
        } catch (Exception e) {
            throw new RuntimeException("Error converting attribute to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    @Nullable
    @SneakyThrows
    public T convertToEntityAttribute(@Nullable String dbData) {
        try {
            return dbData != null ? reader.readValue(dbData) : null;
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to attribute: " + e.getMessage(), e);
        }
    }
}
