package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.ReactionInput;

public class ReactionInputAssert extends AbstractReactionRowAssert<ReactionInput, ReactionInputAssert> {

    public static ReactionInputAssert assertThat(ReactionInput input) {
        return new ReactionInputAssert(input);
    }

    public ReactionInputAssert(ReactionInput reactionInput) {
        super(reactionInput, ReactionInputAssert.class);
    }
}
