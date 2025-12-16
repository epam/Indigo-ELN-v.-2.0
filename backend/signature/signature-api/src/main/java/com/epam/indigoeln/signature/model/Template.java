package com.epam.indigoeln.signature.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class Template {
    @NotNull Integer id;
    @NotEmpty String name;
    @NotEmpty String author;
    @NotNull ZonedDateTime createdDate;
    @NotNull ZonedDateTime lastModifiedDate;
    @NotEmpty List<TemplateSignatureBlock> signatureBlocks;
}
