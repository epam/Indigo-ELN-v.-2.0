package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.reaction.model.CompoundRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class SampleRegistrationRequest {

    @NotNull
    private CompoundRef compound;
}
