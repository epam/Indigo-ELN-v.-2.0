package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryItemDTO {

    @NotNull
    private UUID id;

    @NotNull
    ZonedDateTime createdAt;

    @NotEmpty
    private String name;

    @Nullable
    private String description;

    @NotNull
    @PositiveOrZero
    private Integer ordinal;

    @NotNull
    private Boolean active;

    @Override
    public String toString() {
        return name;
    }
}
