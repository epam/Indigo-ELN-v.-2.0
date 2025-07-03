package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryItemDTO {

    @NotNull
    private UUID id;

    @NotEmpty
    private String name;

    private String description;

    @NotNull
    private Boolean deleted;

    @Override
    public String toString() {
        return name;
    }
}
