package com.epam.indigoeln.compound.model.search;

import com.epam.indigoeln.compound.model.SampleDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record SampleSearchResult (
        List<SampleDTO> items,
        @Nullable Long totalItems,
        @Nullable FindSamplesState next
) {}
