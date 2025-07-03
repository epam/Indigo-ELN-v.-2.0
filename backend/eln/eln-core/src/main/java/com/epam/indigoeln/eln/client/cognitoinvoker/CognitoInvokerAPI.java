package com.epam.indigoeln.eln.client.cognitoinvoker;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "cognito-invoker")
public interface CognitoInvokerAPI {

    @POST
    @Path("/api/cognito-invoker/createUser")
    void createUser(CognitoInvokerCreateUserRequest request);
}
