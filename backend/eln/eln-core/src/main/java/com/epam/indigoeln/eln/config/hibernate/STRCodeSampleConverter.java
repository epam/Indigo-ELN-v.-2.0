package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.model.STRCodeSample;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

@Converter
public class STRCodeSampleConverter implements AttributeConverter<STRCodeSample, String> {

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable STRCodeSample attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    @Nullable
    public STRCodeSample convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null ? STRCodeSample.parse(dbData) : null;
    }
}
