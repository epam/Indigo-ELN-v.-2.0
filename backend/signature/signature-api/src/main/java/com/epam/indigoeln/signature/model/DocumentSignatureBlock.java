package com.epam.indigoeln.signature.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class DocumentSignatureBlock {
    @NotEmpty String user;
    @NotNull Reason reason;
    ZonedDateTime actionDate;
    @NotNull SignatureStatus status;
    String comment;
    boolean canSignOrReject;
//        boolean inspected
}
