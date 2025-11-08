package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.HasDictionaryRefs;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

@Data
public class ExternalSupplier implements HasDictionaryRefs {

    @NotNull
    private DictionaryItemRef supplier;

    @NotNull
    private String registryNumber;

    @Override
    public Stream<@Nullable DictionaryItemRef> collectDictionaryRefs() {
        return Stream.of(supplier);
    }
}
