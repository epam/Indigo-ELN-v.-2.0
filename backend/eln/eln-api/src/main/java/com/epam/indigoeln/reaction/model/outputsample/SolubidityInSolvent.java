package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.eln.model.SolventRef;
import com.epam.indigoeln.reaction.model.ComparisonOperator;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@EqualsAndHashCode
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SolubidityInSolvent.Quantitative.class, name = "QUANTITATIVE"),
        @JsonSubTypes.Type(value = SolubidityInSolvent.Qualitative.class, name = "QUALITATIVE"),
})
public abstract class SolubidityInSolvent {

    @NotNull
    protected SolventRef solvent;

    @Nullable
    protected String comment;

    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = true)
    public static class Quantitative extends SolubidityInSolvent {

        @Nullable
        private ComparisonOperator operator;

        @Nullable
        private Double value;

        @Nullable
        private DensityUnit unit;

        public Quantitative(SolventRef solvent, @Nullable String comment, ComparisonOperator operator, Double value, DensityUnit unit) {
            this.solvent = solvent;
            this.comment = comment;
            this.operator = operator;
            this.value = value;
            this.unit = unit;
        }
    }

    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = true)
    public static class Qualitative extends SolubidityInSolvent {

        @Nullable
        private SolubidityQualitativeType qualitativeType;

        public Qualitative(SolventRef solvent, @Nullable String comment, SolubidityQualitativeType qualitativeType) {
            this.solvent = solvent;
            this.comment = comment;
            this.qualitativeType = qualitativeType;
        }
    }
}
