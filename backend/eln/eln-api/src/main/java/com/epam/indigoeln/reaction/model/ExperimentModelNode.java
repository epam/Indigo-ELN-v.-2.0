package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;

import java.util.function.Consumer;

public sealed interface ExperimentModelNode permits ExperimentModel, Reaction, ReactionRow, ReactionSample, ReactionInput, ReactionInputSample, ReactionOutput, ReactionOutputSample {

    default void collectDictionaries(Consumer<DictionaryItemRef> consumer) {
    }
}
