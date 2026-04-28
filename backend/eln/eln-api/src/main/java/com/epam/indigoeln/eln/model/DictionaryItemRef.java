package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties({"code", "formula", "charge", "molWeight"}) // TODO remove when database is recreated
public abstract class DictionaryItemRef {

    @NotNull
    private UUID id;

    @NotEmpty
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
