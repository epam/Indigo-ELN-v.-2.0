package com.epam.indigoeln.sampleregistration.config;

import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

@Converter
public class STRCodeCompoundConverter implements AttributeConverter<STRCodeCompound, String> {

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable STRCodeCompound attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    @Nullable
    public STRCodeCompound convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null ? STRCodeCompound.parse(dbData) : null;
    }
}
