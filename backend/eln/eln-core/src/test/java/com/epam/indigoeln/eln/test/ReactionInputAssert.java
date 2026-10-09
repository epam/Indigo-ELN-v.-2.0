package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;

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
        EnteredValueAssert.assertThat(actual.getMol()).isEmpty();
        return this;
    }

    public ReactionInputAssert hasWeight(double weight, WeightUnit unit) {
        EnteredValueAssert.assertThat(actual.getWeight()).hasValue(weight, unit);
        return this;
    }

    public ReactionInputAssert hasNoWeight() {
        EnteredValueAssert.assertThat(actual.getWeight()).isEmpty();
        return this;
    }

    public ReactionInputAssert hasVolume(double volume, VolumeUnit unit) {
        EnteredValueAssert.assertThat(actual.getVolume()).hasValue(volume, unit);
        return this;
    }

    public ReactionInputAssert hasNoVolume() {
        EnteredValueAssert.assertThat(actual.getVolume()).isEmpty();
        return this;
    }
}
