package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.CompoundRef;
import org.assertj.core.api.AbstractAssert;

public class CompoundRefAssert extends AbstractAssert<CompoundRefAssert, CompoundRef> {

    public static CompoundRefAssert assertThat(CompoundRef ref) {
        return new CompoundRefAssert(ref);
    }

    public CompoundRefAssert(CompoundRef ref) {
        super(ref, CompoundRefAssert.class);
    }

    public CompoundRefAssert hasMolWeight(double value) {
        EnteredValueAssert.assertThat(actual.getMolWeight()).hasValue(value);
        return this;
    }
}
