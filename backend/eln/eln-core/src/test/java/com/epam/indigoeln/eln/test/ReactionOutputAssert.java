package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;

public class ReactionOutputAssert extends AbstractReactionRowAssert<ReactionOutput, ReactionOutputAssert> {

    public static ReactionOutputAssert assertThat(ReactionOutput output) {
        return new ReactionOutputAssert(output);
    }

    public ReactionOutputAssert(ReactionOutput reactionOutput) {
        super(reactionOutput, ReactionOutputAssert.class);
    }

    public ReactionOutputAssert hasTheoMol(double theoMol, MolUnit unit) {
        EnteredValueAssert.assertThat(actual.getTheoMol()).hasValue(theoMol, unit);
        return this;
    }

    public ReactionOutputAssert hasTheoWeight(double theoWight, WeightUnit unit) {
        EnteredValueAssert.assertThat(actual.getTheoWeight()).hasValue(theoWight, unit);
        return this;
    }
}
