package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.AccessLevel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ACLEntry implements Serializable {

    @NotNull
    private UUID userId;

    @NotEmpty
    private String displayName;

    @NotNull
    private AccessLevel level;

    @NotNull
    private Boolean inherited;
}
