package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(exclude = "reaction")
public sealed abstract class ReactionRow implements ExperimentModelNode permits ReactionInput, ReactionOutput {

    @JsonBackReference
    protected Reaction reaction;

    @NotNull
    protected CompoundRef compound;

    @NotNull
    protected EnteredValue<NoUnit> eq;
}
