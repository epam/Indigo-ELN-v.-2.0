package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.HasDictionaryRefs;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

@Data
public class ResidualSolvent implements HasDictionaryRefs {

    @NotNull
    private DictionaryItemRef solvent;

    @NotNull
    private Double eq;

    @Nullable
    private String comment;

    @Override
    public Stream<@Nullable DictionaryItemRef> collectDictionaryRefs() {
        return Stream.of(solvent);
    }
}
