package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
public class ACLEntryDTO {

    @NotNull
    private UUID userId;

    @NotNull
    private String displayName;

    @NotNull
    private AccessLevel level;

    @NotNull
    private Boolean inherited;
}
