package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.eln.model.NotebookBatchNumber;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class SampleRegistrationRequest {

    @NotNull
    private CompoundRef compound;

    @Nullable
    private NotebookBatchNumber notebookBatchNumber;
}
