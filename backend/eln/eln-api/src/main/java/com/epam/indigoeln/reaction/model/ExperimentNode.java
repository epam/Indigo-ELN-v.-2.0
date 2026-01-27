package com.epam.indigoeln.reaction.model;

public sealed interface ExperimentNode permits ExperimentSnapshot, ExperimentModel, Reaction, ReactionRow, ReactionSample, ReactionInput, ReactionInputSample, ReactionOutput, ReactionOutputSample {
}
