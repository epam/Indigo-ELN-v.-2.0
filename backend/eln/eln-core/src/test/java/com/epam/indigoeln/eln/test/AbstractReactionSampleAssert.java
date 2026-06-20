package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.ReactionRow;
import com.epam.indigoeln.reaction.model.ReactionSample;
import com.epam.indigoeln.reaction.model.units.VolumeUnit;
import org.assertj.core.api.AbstractAssert;

public class AbstractReactionSampleAssert<R extends ReactionRow, S extends ReactionSample<R>, SELF extends AbstractReactionSampleAssert<R, S, SELF>> extends AbstractAssert<SELF, S> {

    protected AbstractReactionSampleAssert(S sample, Class<SELF> selfType) {
        super(sample, selfType);
    }

    public SELF hasVolume(double volume, VolumeUnit unit) {
        EnteredValueAssert.assertThat(actual.getVolume()).hasValue(volume, unit);
        return myself;
    }
}
