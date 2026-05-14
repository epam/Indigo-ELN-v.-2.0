package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.util.ExperimentModelUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@EqualsAndHashCode(exclude = "reaction", callSuper = false)
public sealed abstract class ReactionRow extends AbstractExperimentNode<Reaction> permits ReactionInput, ReactionOutput {

    @JsonBackReference
    protected Reaction reaction;

    @NotNull
    @Setter(AccessLevel.PROTECTED)
    protected CompoundRef compound;

    @NotNull
    protected EnteredValue<NoUnit> eq; // = 1

    @Nullable
    protected Integer rxnPosition;

    @Override
    protected Reaction internalGetParent() {
        return reaction;
    }

    @Override
    protected void internalSetParent(Reaction parent) {
        reaction = parent;
    }

    @JsonIgnore
    @AssertTrue(message = "invalid rxnPosition")
    protected boolean isRxnPositionValid() {
        boolean rxnPositionExpected = ExperimentModelUtil.getRoleInSchema(this) != null;
        return (rxnPosition != null) == rxnPositionExpected;
    }
}
