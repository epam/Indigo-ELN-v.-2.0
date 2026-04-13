package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ACLDetailsEntryDTO extends ACLEntryDTO {

    @NotNull
    private String username;

    public ACLDetailsEntryDTO(UUID userId, String displayName, AccessLevel level, Boolean inherited, String username) {
        super(userId, displayName, level, inherited);
        this.username = username;
    }
}
