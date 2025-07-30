package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.MoreObjects;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompoundRef.Stored.class, name = "stored"),
        @JsonSubTypes.Type(value = CompoundRef.Virtual.class, name = "virtual"),
        @JsonSubTypes.Type(value = CompoundRef.Unknown.class, name = "unknown")
})
public sealed interface CompoundRef permits CompoundRef.Stored, CompoundRef.Virtual, CompoundRef.Unknown {

    @Nullable
    String getMolFile();

    String getFormula();

    @Nullable
    EnteredValue<MolWeightUnit> getMolWeight();

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"compoundID", "stereoisomerCode", "saltCode", "saltEQ"})
    final class Stored implements CompoundRef {

        @NotNull
        private final UUID compoundID;

        @Nullable
        private DictionaryItemRef stereoisomerCode;

        @Nullable
        private SaltCodeRef saltCode;

        @Nullable
        private Double saltEQ;

        @Nullable
        private final String name;

        @NotNull
        private final EnteredValue<MolWeightUnit> molWeight;

        @NotNull
        private final String molFile;

        @NotNull
        private final String formula;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).omitNullValues()
                    .add("compoundID", compoundID)
                    .add("name", name)
                    .add("molWeight", molWeight)
                    .add("formula", formula)
                    .toString();
        }
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"molFile", "stereoisomerCode", "saltCode", "saltEQ"})
    final class Virtual implements CompoundRef {

        @NotNull
        private final UUID compoundID;

        @NotNull
        private final String molFile;

        @NotNull
        private final String formula;

        @Nullable
        private final DictionaryItemRef stereoisomerCode;

        @Nullable
        private final SaltCodeRef saltCode;

        @Nullable
        private final Double saltEQ;

        @NotNull
        @Positive
        private EnteredValue<MolWeightUnit> molWeight;

        public Virtual(UUID compoundID, String molFile, String formula, Double molWeight) {
            this(compoundID, molFile, formula, EnteredValue.fixed(molWeight, MolWeightUnit.G_PER_MOL), null, null, null);
        }

        @JsonCreator
        Virtual(UUID compoundID, String molFile, String formula, EnteredValue<MolWeightUnit> molWeight, @Nullable DictionaryItemRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ) {
            this.compoundID = compoundID;
            this.molFile = molFile;
            this.formula = formula;
            this.molWeight = molWeight;
            this.stereoisomerCode = stereoisomerCode;
            this.saltCode = saltCode;
            this.saltEQ = saltEQ;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).omitNullValues()
                    .add("compoundID", compoundID)
                    .add("formula", formula)
                    .add("saltCode", saltCode)
                    .add("saltEQ", saltEQ)
                    .add("molWeight", molWeight)
                    .toString();
        }
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    // no equals and hashCode - each unknown compound is unique
    final class Unknown implements CompoundRef {

        private String formula;

        @Nullable
        private EnteredValue<MolWeightUnit> molWeight;

        @Override
        @Nullable
        @JsonIgnore
        public String getMolFile() {
            return null;
        }
    }
}
