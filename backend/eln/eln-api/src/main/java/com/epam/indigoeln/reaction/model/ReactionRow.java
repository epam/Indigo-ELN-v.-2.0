package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public sealed abstract class ReactionRow implements ExperimentModelNode, ToStringTree permits ReactionInput, ReactionOutput {

    @JsonBackReference
    protected Reaction reaction;

    @NotNull
    protected CompoundRef compound;

    @NotNull
    protected EnteredValue<NoUnit> eq;

    @Override
    public void prepareToRecalculate() {
        EnteredValue.prepareToRecalculate(eq, this::setEq, EnteredValue.DEFAULT_ONE);
    }

    @Override
    public String toString() {
        return toStringTree();
    }
}
