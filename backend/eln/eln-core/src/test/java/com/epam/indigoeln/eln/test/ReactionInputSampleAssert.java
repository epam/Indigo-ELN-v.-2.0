package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;

public class ReactionInputSampleAssert extends AbstractReactionSampleAssert<ReactionInput, ReactionInputSample, ReactionInputSampleAssert> {

    public static ReactionInputSampleAssert assertThat(ReactionInputSample input) {
        return new ReactionInputSampleAssert(input);
    }

    public ReactionInputSampleAssert(ReactionInputSample reactionInputSample) {
        super(reactionInputSample, ReactionInputSampleAssert.class);
    }

    public ReactionInputSampleAssert hasMol(double mol, MolUnit unit) {
        EnteredValueAssert.assertThat(actual.getMol()).hasValue(mol, unit);
        return this;
    }

    public ReactionInputSampleAssert hasWeight(double weight, WeightUnit unit) {
        EnteredValueAssert.assertThat(actual.getWeight()).hasValue(weight, unit);
        return this;
    }
}
