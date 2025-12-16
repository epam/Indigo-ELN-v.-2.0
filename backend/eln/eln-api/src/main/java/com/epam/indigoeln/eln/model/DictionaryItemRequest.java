package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryItemRequest {

    @NotEmpty
    private String name;

    @Nullable
    private String description;

    @Override
    public String toString() {
        return name;
    }
}
