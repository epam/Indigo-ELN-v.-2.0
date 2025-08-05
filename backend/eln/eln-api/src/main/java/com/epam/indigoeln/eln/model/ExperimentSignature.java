package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
public class ExperimentSignature {

    @NotNull
    UserRef user;

    @NotNull
    SignatureReason reason;

    @Nullable
    SignatureStatus status;
}
