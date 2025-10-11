package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.model.NotebookBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeCompound;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

@Converter
public class NotebookBatchNumberConverter implements AttributeConverter<NotebookBatchNumber, String> {

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable NotebookBatchNumber attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    @Nullable
    public NotebookBatchNumber convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null ? NotebookBatchNumber.parse(dbData) : null;
    }
}
