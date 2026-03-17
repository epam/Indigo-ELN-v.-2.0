package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
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
    protected CompoundRef compound;

    @NotNull
    protected EnteredValue<NoUnit> eq;

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
}
