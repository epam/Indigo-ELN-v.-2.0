package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompoundRef.Stored.class, name = CompoundRef.Stored.TYPE),
        @JsonSubTypes.Type(value = CompoundRef.Virtual.class, name = CompoundRef.Virtual.TYPE),
        @JsonSubTypes.Type(value = CompoundRef.Unknown.class, name = CompoundRef.Unknown.TYPE)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface CompoundRef permits CompoundRef.StoredOrVirtual, CompoundRef.Unknown {

    @Nullable
    UUID getCompoundID();

    @Nullable
    String getFormula();

    @Nullable
    StereoisomerCodeRef getStereoisomerCode();

    @Nullable
    SaltCodeRef getSaltCode();

    @Nullable
    Double getSaltEQ();

    @Nullable
    String getCompoundKey();

    @Nullable
    EnteredValue<MolWeightUnit> getMolWeight();

    @Nullable
    EnteredValue<NoUnit> getExactMass();

    @Nullable
    String getCasNumber();

    @Nullable
    String getCalculatedBatchMF();

    default boolean compoundKeyEquals(CompoundRef other) {
        // for stored and virtual compound, compound identity already checked when assigning compoundID; thus can only compare compoundID;
        // unknown compound (with compoundID null) only equals to itself
        return this == other || (getCompoundID() != null && getCompoundID().equals(other.getCompoundID()));
    }

    sealed interface StoredOrVirtual extends CompoundRef permits CompoundRef.Stored, CompoundRef.Virtual {

        UUID getCompoundID();

        EnteredValue<MolWeightUnit> getMolWeight();

        EnteredValue<NoUnit> getExactMass();

        String getFormula();

        String getCalculatedBatchMF();
    }

    @Getter
    @ToString
    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"compoundID"})
    final class Stored implements CompoundRef.StoredOrVirtual {

        public static final String TYPE = "STORED";

        @NotNull
        private final UUID compoundID;

        @Nullable
        private final StereoisomerCodeRef stereoisomerCode;

        @Nullable
        private final SaltCodeRef saltCode;

        @Nullable
        private final Double saltEQ;

        @NotNull
        @Positive
        private final EnteredValue<MolWeightUnit> molWeight;

        @NotNull
        private final EnteredValue<NoUnit> exactMass;

        @NotNull
        private final String formula;

        @Nullable
        private final String compoundKey;

        @Nullable
        private final String casNumber;

        @NotNull
        private final String calculatedBatchMF;
    }

    @Getter
    @ToString
    @EqualsAndHashCode(of = {"compoundID"})
    @AllArgsConstructor
    final class Virtual implements CompoundRef.StoredOrVirtual {

        public static final String TYPE = "VIRTUAL";

        @NotNull
        private final UUID compoundID;

        @NotNull
        private final String formula;

        @Nullable
        private final String compoundKey;

        @Nullable
        private final StereoisomerCodeRef stereoisomerCode;

        @Nullable
        private final SaltCodeRef saltCode;

        @Nullable
        private final Double saltEQ;

        @NotNull
        @Positive
        private final EnteredValue<MolWeightUnit> molWeight;

        @NotNull
        private final EnteredValue<NoUnit> exactMass;

        @Nullable
        private final String casNumber;

        @NotNull
        private final String calculatedBatchMF;
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    final class Unknown implements CompoundRef {

        public static final String TYPE = "UNKNOWN";

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
        public StereoisomerCodeRef getStereoisomerCode() {
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
        public String getCompoundKey() {
            return null;
        }

        @Override
        @Nullable
        @JsonIgnore
        public EnteredValue<NoUnit> getExactMass() {
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
