package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.ComparisonOperator;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class SolubidityInSolvent {

    @NotNull
    private DictionaryItemRef solvent;

    @Nullable
    private String comment;

    @NotNull
    private SolubidityType solubidityType;

    @Nullable
    private ComparisonOperator operator;

    @Nullable
    private Double value;

    @Nullable
    private DensityUnit unit;

    @Nullable
    private SolubidityQualitativeType qualitativeType;

    @JsonIgnore
    @AssertTrue(message = "operator, value and unit are only allowed for quantitative solubidity")
    public boolean isQuantitativeFieldsValid() {
        if (solubidityType == SolubidityType.QUANTITATIVE) {
            return qualitativeType == null;
        }
        return true;
    }

    @JsonIgnore
    @AssertTrue(message = "operator, value and unit are required for quantitative solubidity")
    public boolean isQuantitativeFieldsSet() {
        if (solubidityType == SolubidityType.QUANTITATIVE) {
            return operator != null && value != null && unit != null;
        }
        return true;
    }

    @JsonIgnore
    @AssertTrue(message = "qualitativeType are only allowed for qualitative solubidity")
    public boolean isQualitativeFieldsValid() {
        if (solubidityType == SolubidityType.QUALITATIVE) {
            return qualitativeType == null;
        }
        return true;
    }

    @JsonIgnore
    @AssertTrue(message = "operator, value and unit are required for quantitative solubidity")
    public boolean isQualitativeFieldsSet() {
        if (solubidityType == SolubidityType.QUALITATIVE) {
            return qualitativeType != null;
        }
        return true;
    }
}
