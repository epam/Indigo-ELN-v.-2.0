package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.STRCodeCompound;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.MoreObjects;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompoundRef.Stored.class, name = CompoundRef.Stored.TYPE),
        @JsonSubTypes.Type(value = CompoundRef.Virtual.class, name = CompoundRef.Virtual.TYPE),
        @JsonSubTypes.Type(value = CompoundRef.Unknown.class, name = CompoundRef.Unknown.TYPE)
})
public sealed interface CompoundRef permits CompoundRef.Stored, CompoundRef.Virtual, CompoundRef.Unknown {

    @Nullable
    UUID getCompoundID();

    @Nullable
    String getMolFile();

    @Nullable
    String getFormula();

    @Nullable
    DictionaryItemRef getStereoisomerCode();

    @Nullable
    SaltCodeRef getSaltCode();

    @Nullable
    Double getSaltEQ();

    @Nullable
    STRCodeCompound getStrCode();

    @Nullable
    EnteredValue<MolWeightUnit> getMolWeight();

    @Nullable
    Double getExactMass();

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"compoundID"})
    final class Stored implements CompoundRef {

        public static final String TYPE = "stored";

        @NotNull
        private final UUID compoundID;

        @Nullable
        private DictionaryItemRef stereoisomerCode;

        @Nullable
        private SaltCodeRef saltCode;

        @Nullable
        private Double saltEQ;

        @NotNull
        private final EnteredValue<MolWeightUnit> molWeight;

        @NotNull
        private final Double exactMass;

        @NotNull
        private final String molFile;

        @NotNull
        private final String formula;

        @Nullable
        private final STRCodeCompound strCode;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).omitNullValues()
                    .add("compoundID", compoundID)
                    .add("molWeight", molWeight)
                    .add("formula", formula)
                    .toString();
        }
    }

    @Getter
    @EqualsAndHashCode(of = {"compoundID"})
    final class Virtual implements CompoundRef {

        public static final String TYPE = "virtual";

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

        @NotNull
        private Double exactMass;

        public Virtual(UUID compoundID, String molFile, String formula, Double molWeight, Double exactMass) {
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
        @Nullable
        @JsonIgnore
        public STRCodeCompound getStrCode() {
            return null;
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
    @EqualsAndHashCode
    final class Unknown implements CompoundRef {

        public static final String TYPE = "unknown";

        @Nullable
        private String formula;

        @Nullable
        @Setter
        private EnteredValue<MolWeightUnit> molWeight;

        @Override
        @Nullable
        @JsonIgnore
        public UUID getCompoundID() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public String getMolFile() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public DictionaryItemRef getStereoisomerCode() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public SaltCodeRef getSaltCode() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public Double getSaltEQ() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public STRCodeCompound getStrCode() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public Double getExactMass() {
            return null;
        }
    }
}
