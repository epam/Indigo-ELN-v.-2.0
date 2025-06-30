package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.client.cognitoinvoker.CognitoInvokerAPI;
import com.epam.indigoeln.eln.client.cognitoinvoker.CognitoInvokerCreateUserRequest;
import com.epam.indigoeln.eln.model.UserRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Alternative
@ApplicationScoped
public class CognitoInvokerExternalUserService implements ExternalUserService {

    @Inject
    @RestClient
    CognitoInvokerAPI cognitoInvokerAPI;

    @ConfigProperty(name = "eln.cognito-invoker.user-pool-id")
    String userPoolId;

    public void createUser(UserRequest request) {
        try {
            cognitoInvokerAPI.createUser(new CognitoInvokerCreateUserRequest(
                    userPoolId,
                    request.getUsername(),
                    request.getPassword()
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user in Cognito", e);
        }
    }

//    public void deleteUser(String email) {
//        var request = AdminDeleteUserRequest.builder()
//                .userPoolId(userPoolId)
//                .username(email)
//                .build();
//
//        cognitoClient.adminDeleteUser(request);
//    }
//
//    public void resetPassword(String email) {
//        var request = AdminResetUserPasswordRequest.builder()
//                .userPoolId(userPoolId)
//                .username(email)
//                .build();
//
//        cognitoClient.adminResetUserPassword(request);
//    }
}
