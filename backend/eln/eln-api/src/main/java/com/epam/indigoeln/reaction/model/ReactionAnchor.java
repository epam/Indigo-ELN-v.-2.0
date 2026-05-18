package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.UUID;

public class ReactionAnchor extends Anchor {

    public ReactionAnchor(UUID value) {
        super(value);
    }

    public static ReactionAnchor create() {
        return new ReactionAnchor(UUID.randomUUID());
    }

    @JsonCreator
    public ReactionAnchor(String str) {
        super(str);
    }
}
