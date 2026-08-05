package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;

public class ReactionOutputSampleAssert extends AbstractReactionSampleAssert<ReactionOutput, ReactionOutputSample, ReactionOutputSampleAssert> {

    public static ReactionOutputSampleAssert assertThat(ReactionOutputSample sample) {
        return new ReactionOutputSampleAssert(sample);
    }

    public ReactionOutputSampleAssert(ReactionOutputSample sample) {
        super(sample, ReactionOutputSampleAssert.class);
    }

    public ReactionOutputSampleAssert hasActualMol(double actualMol, MolUnit unit) {
        EnteredValueAssert.assertThat(actual.getActualMol()).hasValue(actualMol, unit);
        return this;
    }

    public ReactionOutputSampleAssert hasNoActualMol() {
        EnteredValueAssert.assertThat(actual.getActualMol()).isEmpty();
        return this;
    }

    public ReactionOutputSampleAssert hasActualWeight(double actualWeight, WeightUnit unit) {
        EnteredValueAssert.assertThat(actual.getActualWeight()).hasValue(actualWeight, unit);
        return this;
    }

    public ReactionOutputSampleAssert hasNoActualWeight() {
        EnteredValueAssert.assertThat(actual.getActualWeight()).isEmpty();
        return this;
    }

    public ReactionOutputSampleAssert hasYield(double yieldValue) {
        EnteredValueAssert.assertThat(actual.getYieldValue()).hasValue(yieldValue);
        return this;
    }

    public ReactionOutputSampleAssert hasNoYield() {
        EnteredValueAssert.assertThat(actual.getYieldValue()).isEmpty();
        return this;
    }
}
