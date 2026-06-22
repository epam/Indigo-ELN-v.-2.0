package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.units.MolUnit;

public class ReactionInputAssert extends AbstractReactionRowAssert<ReactionInput, ReactionInputAssert> {

    public static ReactionInputAssert assertThat(ReactionInput input) {
        return new ReactionInputAssert(input);
    }

    public ReactionInputAssert(ReactionInput reactionInput) {
        super(reactionInput, ReactionInputAssert.class);
    }

    public ReactionInputAssert hasMol(double mol, MolUnit unit) {
        EnteredValueAssert.assertThat(actual.getMol()).hasValue(mol, unit);
        return this;
    }

    public ReactionInputAssert hasNoMol() {
        EnteredValueAssert.assertThat(actual.getMol()).isNull();
        return this;
    }
}
