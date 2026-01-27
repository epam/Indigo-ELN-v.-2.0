package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryDTO extends BaseDTO {

    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @NotNull
    private Boolean userEditable;

    @NotEmpty
    private String description;

    @Override
    public String toString() {
        return name;
    }
}
