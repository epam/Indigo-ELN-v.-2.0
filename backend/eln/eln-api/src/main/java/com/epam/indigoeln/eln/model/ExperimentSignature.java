package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;

@Value
public class ExperimentSignature {

    @NotNull
    UserRef user;

    @NotNull
    SignatureReason reason;

    @Nullable
    SignatureStatus status;

    @Nullable
    ZonedDateTime signedAt;
}
