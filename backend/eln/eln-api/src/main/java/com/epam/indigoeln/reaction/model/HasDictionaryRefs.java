package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

public interface HasDictionaryRefs {

    Stream<@Nullable DictionaryItemRef> collectDictionaryRefs();
}
