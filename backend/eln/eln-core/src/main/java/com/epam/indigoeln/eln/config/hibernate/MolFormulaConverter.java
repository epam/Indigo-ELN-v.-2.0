package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.reaction.model.MolFormula;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

@Converter
public class MolFormulaConverter implements AttributeConverter<MolFormula, String> {

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable MolFormula attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    @Nullable
    public MolFormula convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null ? new MolFormula(dbData) : null;
    }
}
