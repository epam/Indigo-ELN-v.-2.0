package com.epam.indigoeln.eln.common.config;

import com.epam.indigoeln.common.model.NbkBatchNumber;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

@Converter
public class NbkBatchNumberConverter implements AttributeConverter<NbkBatchNumber, String> {

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable NbkBatchNumber attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    @Nullable
    public NbkBatchNumber convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null ? NbkBatchNumber.parse(dbData) : null;
    }
}
