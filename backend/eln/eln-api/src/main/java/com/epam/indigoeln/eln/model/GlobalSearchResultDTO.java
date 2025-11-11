package com.epam.indigoeln.eln.model;

import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class GlobalSearchResultDTO extends BaseDTO {

    private EntityType type;
    private String name;
    @Nullable
    private String fragment;
}
