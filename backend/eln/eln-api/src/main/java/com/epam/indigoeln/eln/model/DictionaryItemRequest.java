package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryItemRequest {

    @Nullable
    private UUID id;

    @NotEmpty
    private String name;

    @Nullable
    private String description;

    @Nullable
    private Boolean deleted;

    @Override
    public String toString() {
        return name;
    }
}
