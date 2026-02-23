package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
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
    private Patched<BigDecimal, BigDecimal> exactMass;

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
