package com.epam.indigoeln.signature.model;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.common.model.UserRef;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

    @NotNull
    private UUID id;

    @NotEmpty
    private String name;

    @NotNull
    private DocumentStatus status;

    @NotNull
    private ZonedDateTime createdDate;

    @NotNull
    private ZonedDateTime lastModifiedDate;

    @NotNull
    private UserRef author;

    @NotNull
    private List<DocumentSignatureDTO> signatures;
}
