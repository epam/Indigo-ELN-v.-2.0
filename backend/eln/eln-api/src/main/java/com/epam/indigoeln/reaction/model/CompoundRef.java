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
import lombok.*;
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
    String getFormula();

    @Nullable
    DictionaryItemRef getStereoisomerCode();

    @Nullable
    DictionaryItemRef getSaltCode();

    @Nullable
    Double getSaltEQ();

    @Nullable
    String getCompoundKey();

    @Nullable
    EnteredValue<MolWeightUnit> getMolWeight();

    @Nullable
    Double getExactMass();

    @Nullable
    String getCasNumber();

    @Nullable
    String getCalculatedBatchMF();

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
        private DictionaryItemRef saltCode;

        @Nullable
        private Double saltEQ;

        @NotNull
        private final EnteredValue<MolWeightUnit> molWeight;

        @NotNull
        private final Double exactMass;

        @NotNull
        private final String formula;

        @Nullable
        private final String compoundKey;

        @Nullable
        private final String casNumber;

        @NotNull
        private final String calculatedBatchMF;

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
    @AllArgsConstructor(onConstructor_ = @JsonCreator)
    final class Virtual implements CompoundRef {

        public static final String TYPE = "virtual";

        @NotNull
        private final UUID compoundID;

        @NotNull
        private final String formula;

        @Nullable
        private final String compoundKey;

        @Nullable
        private final DictionaryItemRef stereoisomerCode;

        @Nullable
        private final DictionaryItemRef saltCode;

        @Nullable
        private final Double saltEQ;

        @NotNull
        private EnteredValue<MolWeightUnit> molWeight;

        @NotNull
        private Double exactMass;

        @Nullable
        private final String casNumber;

        @NotNull
        private final String calculatedBatchMF;

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
        @Setter
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
        public DictionaryItemRef getStereoisomerCode() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public DictionaryItemRef getSaltCode() {
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
        public String getCompoundKey() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public Double getExactMass() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public String getCasNumber() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public String getCalculatedBatchMF() {
            return null;
        }
    }
}
