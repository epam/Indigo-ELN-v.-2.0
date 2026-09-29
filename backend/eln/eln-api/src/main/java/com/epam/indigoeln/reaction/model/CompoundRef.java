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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
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
    private final EnteredValue<NoUnit> exactMass;

    @Nullable
    @SuppressWarnings("unused") // used on frontend
    private final String casNumber;

    public CompoundRef() {
        this(null, null, null, null, null, null, EnteredValue.empty(), EnteredValue.empty(), null);
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

    public void setMolWeight(@Nullable EnteredValue<MolWeightUnit> molWeight) {
        checkState(!isKnown());
        this.molWeight = molWeight;
    }
}
