package com.epam.indigoeln.eln.client.cognitoinvoker;

import org.jspecify.annotations.Nullable;

public record CognitoInvokerCreateUserRequest(
        String userPoolId,
        String username,
        @Nullable
        String password
) {
}
