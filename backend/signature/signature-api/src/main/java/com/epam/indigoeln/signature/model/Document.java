package com.epam.indigoeln.signature.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class Document {
    @NotNull Integer id;
    @NotEmpty String name;
    @NotNull Status status;
    @NotNull ZonedDateTime createdDate;
    @NotNull ZonedDateTime lastModifiedDate;
    @NotEmpty String author;
    @NotEmpty List<DocumentSignatureBlock> signatureBlocks;
//       boolean actionRequired,
//       boolean inspected
}
