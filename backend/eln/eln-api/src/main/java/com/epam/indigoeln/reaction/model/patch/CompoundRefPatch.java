package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompoundRefPatch {

    @Nullable
    private Patched<Type, Type> type;

    @Nullable
    private Patched<UUID, UUID> compoundID;

    @Nullable
    private Patched<String, String> formula;

    @Nullable
    private Patched<DictionaryItemRef, DictionaryItemRef> stereoisomerCode;

    @Nullable
    private Patched<DictionaryItemRef, DictionaryItemRef> saltCode;

    @Nullable
    private Patched<Double, Double> saltEQ;

    @Nullable
    private Patched<String, String> compoundKey;

    @Nullable
    private Patched<EnteredValue<MolWeightUnit>, EnteredValuePatch<MolWeightUnit>> molWeight;

    @Nullable
    private Patched<Double, Double> exactMass;

    @Nullable
    private Patched<String, String> casNumber;

    @Nullable
    private Patched<String, String> calculatedBatchMF;

    public enum Type {

        STORED,
        VIRTUAL,
        UNKNOWN,
    }
}
