package com.epam.indigoeln.signature.model;

import com.epam.indigoeln.common.model.UserRef;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DocumentSignatureDTO {

    @NotNull
    private UUID id;

    @NotNull
    private UserRef user;

    @NotNull
    private SignatureReason reason;

    @Nullable
    private Instant actionDate;

    @NotNull
    private SignatureStatus status;

    @Nullable
    private String comment;

    boolean canSignOrReject;
}
