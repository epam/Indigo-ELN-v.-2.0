package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.AbstractReactionRowPatch;
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

    protected static <C extends ReactionRow, A extends Anchor, P extends AbstractReactionRowPatch<A>> void buildMetamodelBase(Metamodel<C, P> metamodel) {
        metamodel.simpleProperty("compound", ReactionRow::getCompound, ReactionRow::setCompound, AbstractReactionRowPatch::getCompound, AbstractReactionRowPatch::setCompound);
        metamodel.enteredValueProperty("eq", ReactionRow::getEq, ReactionRow::setEq, AbstractReactionRowPatch::getEq, AbstractReactionRowPatch::setEq);
    }

    @JsonBackReference
    protected Reaction reaction;

    @NotNull
    protected CompoundRef compound;

    @NotNull
    protected EnteredValue<NoUnit> eq;
}
