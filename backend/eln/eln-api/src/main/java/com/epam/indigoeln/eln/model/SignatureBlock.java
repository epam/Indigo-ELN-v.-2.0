package com.epam.indigoeln.eln.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
@RegisterForReflection
public class SignatureBlock {

    @Nullable
    UserRef user;

    @NotNull
    SignatureReason reason;

    @AssertTrue
    boolean isUserValid() {
        return switch (reason) {
            case null -> true;
            case AUTHOR -> user == null;
            case WITNESS -> user != null;
        };
    }
}
