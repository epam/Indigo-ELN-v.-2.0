package com.epam.indigoeln.reaction.model;

public sealed interface ExperimentModelNode permits ExperimentModel, Reaction, ReactionRow, ReactionSample, ReactionInput, ReactionInputSample, ReactionOutput, ReactionOutputSample {
}
