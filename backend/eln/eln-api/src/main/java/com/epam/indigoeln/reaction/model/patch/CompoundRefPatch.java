package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
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
    private Patched<Type> type;
    @Nullable
    private Patched<UUID> compoundID;
    @Nullable
    private Patched<String> formula;
    @Nullable
    private Patched<DictionaryItemRef> stereoisomerCode;
    @Nullable
    private Patched<DictionaryItemRef> saltCode;
    @Nullable
    private Patched<Double> saltEQ;
    @Nullable
    private Patched<String> compoundKey;
    @Nullable
    private Patched<EnteredValuePatch<MolWeightUnit>> molWeight;
    @Nullable
    private Patched<Double> exactMass;
    @Nullable
    private Patched<String> casNumber;
    @Nullable
    private Patched<String> calculatedBatchMF;

    public enum Type {

        STORED,
        VIRTUAL,
        UNKNOWN,
    }
}
