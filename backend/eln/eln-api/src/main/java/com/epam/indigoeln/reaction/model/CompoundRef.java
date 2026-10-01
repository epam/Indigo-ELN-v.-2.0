package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.model.units.MolWeightUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompoundRef {

    @Nullable
    private final UUID compoundID;

    @Nullable
    private final StereoisomerCodeRef stereoisomerCode;

    @Nullable
    private final SaltCodeRef saltCode;

    @Nullable
    private final Double saltEQ;

    @Nullable
    @SuppressWarnings("unused") // used on frontend
    private final String compoundKey;

    @Nullable
    @SuppressWarnings("unused") // used on frontend
    private final MolFormula formula;

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private EnteredValue<MolWeightUnit> molWeight;

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @SuppressWarnings("unused") // used on frontend
    private EnteredValue<NoUnit> exactMass;

    @Nullable
    @SuppressWarnings("unused") // used on frontend
    private final String casNumber;

    public CompoundRef() {
        this(null, null, null, null, null, null, null, null, null);
    }

    @JsonCreator
    public CompoundRef(@Nullable UUID compoundID, @Nullable StereoisomerCodeRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ, @Nullable String compoundKey, @Nullable MolFormula formula, @Nullable EnteredValue<MolWeightUnit> molWeight, @Nullable EnteredValue<NoUnit> exactMass, @Nullable String casNumber) {
        this.compoundID = compoundID;
        this.stereoisomerCode = stereoisomerCode;
        this.saltCode = saltCode;
        this.saltEQ = saltEQ;
        this.compoundKey = compoundKey;
        this.formula = formula;
        this.molWeight = MoreObjects.firstNonNull(molWeight, EnteredValue.empty());
        this.exactMass = MoreObjects.firstNonNull(exactMass, EnteredValue.empty());
        this.casNumber = casNumber;
    }

    public CompoundRef copy() {
        return new CompoundRef(compoundID, stereoisomerCode, saltCode, saltEQ, compoundKey, formula, molWeight, exactMass, casNumber);
    }

    public boolean compoundKeyEquals(CompoundRef other) {
        // for stored and virtual compound, compound identity already checked when assigning compoundID; thus can only compare compoundID;
        // unknown compound (with compoundID null) only equals to itself
        return this == other || (compoundID != null && compoundID.equals(other.compoundID));
    }

    @JsonIgnore
    public boolean isKnown() {
        return compoundID != null;
    }

    @Nullable
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @SuppressWarnings("unused") // used on frontend
    public String getCalculatedBatchMF() {
        if (formula == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String parentFormula = formula.toHTMLString();
        sb.append(parentFormula);
        if (saltCode != null) {
            sb.append("&nbsp;*&nbsp;").append((saltEQ)).append(" (").append(saltCode.getFormula()).append(")");
        }
        return sb.toString();
    }

    public void setMolWeight(EnteredValue<MolWeightUnit> molWeight) {
        checkState(!isKnown());
        this.molWeight = molWeight;
    }
}
