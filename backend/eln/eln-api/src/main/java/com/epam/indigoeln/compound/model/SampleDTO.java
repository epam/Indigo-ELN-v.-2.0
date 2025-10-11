package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.eln.model.NotebookBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeSample;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Data
public class SampleDTO {

    @NotNull
    private UUID id;
    @Nullable
    private NotebookBatchNumber notebookBatchNumber;
    @Nullable
    private STRCodeSample strCode;
}
