package com.epam.indigoeln.signature.model;

import com.epam.indigoeln.common.model.UserRef;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
@RegisterForReflection
public class SignatureTemplateBlock {

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
