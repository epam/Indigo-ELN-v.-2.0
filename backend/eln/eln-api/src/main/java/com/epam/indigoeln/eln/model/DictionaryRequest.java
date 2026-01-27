package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryRequest {

    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @NotEmpty
    private Boolean userEditable;

    @Nullable
    private String description;
}
