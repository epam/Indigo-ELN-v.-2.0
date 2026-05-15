package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
public class ACLEntryDTO {

    @NotNull
    private String username;

    @NotNull
    private String displayName;

    @NotNull
    private AccessLevel level;

    @NotNull
    private Boolean inherited;
}
