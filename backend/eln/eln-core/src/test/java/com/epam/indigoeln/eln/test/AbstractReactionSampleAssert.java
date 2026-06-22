package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.ReactionRow;
import com.epam.indigoeln.reaction.model.ReactionSample;
import com.epam.indigoeln.reaction.model.units.MolarityUnit;
import com.epam.indigoeln.reaction.model.units.VolumeUnit;
import org.assertj.core.api.AbstractAssert;

@SuppressWarnings("UnusedReturnValue")
public class AbstractReactionSampleAssert<R extends ReactionRow, S extends ReactionSample<R>, SELF extends AbstractReactionSampleAssert<R, S, SELF>> extends AbstractAssert<SELF, S> {

    protected AbstractReactionSampleAssert(S sample, Class<SELF> selfType) {
        super(sample, selfType);
    }

    public SELF hasDensity(double density) {
        EnteredValueAssert.assertThat(actual.getDensity()).hasValue(density);
        return myself;
    }

    public SELF hasNoDensity() {
        EnteredValueAssert.assertThat(actual.getDensity()).isNull();
        return myself;
    }

    public SELF hasMolarity(double molarity, MolarityUnit unit) {
        EnteredValueAssert.assertThat(actual.getMolarity()).hasValue(molarity, unit);
        return myself;
    }

    public SELF hasNoMolarity() {
        EnteredValueAssert.assertThat(actual.getMolarity()).isNull();
        return myself;
    }

    public SELF hasVolume(double volume, VolumeUnit unit) {
        EnteredValueAssert.assertThat(actual.getVolume()).hasValue(volume, unit);
        return myself;
    }

    public SELF hasNoVolume() {
        EnteredValueAssert.assertThat(actual.getVolume()).isNull();
        return myself;
    }

    public SELF hasPurity(double purity) {
        EnteredValueAssert.assertThat(actual.getPurity()).hasValue(purity);
        return myself;
    }
}
