package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.ReactionRow;
import org.assertj.core.api.AbstractAssert;

public class AbstractReactionRowAssert<R extends ReactionRow, SELF extends AbstractReactionRowAssert<R, SELF>> extends AbstractAssert<SELF, R> {

    protected AbstractReactionRowAssert(R row, Class<SELF> selfType) {
        super(row, selfType);
    }

    public CompoundRefAssert compound() {
        return new CompoundRefAssert(this.actual.getCompound());
    }
}
