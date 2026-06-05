package com.epam.indigoeln.reaction.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public sealed abstract class AbstractExperimentNode<P extends ExperimentNode> implements ExperimentNode permits Reaction, ReactionRow, ReactionSample {
}
