package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ACLDetailsEntryDTO extends ACLEntryDTO {

    @NotNull
    private String username;
}
