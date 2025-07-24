package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.UserRequest;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
class CognitoExternalUserService implements ExternalUserService {

    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;

    @SneakyThrows
    public CognitoExternalUserService(CognitoIdentityProviderClient cognitoClient, String userPoolId) {
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
    }

    @SneakyThrows
    public void createUser(UserRequest request) {
        try {
            cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(request.getUsername())
                    .userAttributes(
                            AttributeType.builder().name("email").value(request.getUsername()).build(),
                            AttributeType.builder().name("email_verified").value("true").build(),
                            AttributeType.builder().name("given_name").value(request.getFirstName()).build(),
                            AttributeType.builder().name("family_name").value(request.getLastName()).build()
                    )
                    .build());
        } catch (UsernameExistsException ignore) {
        }
        if (request.getPassword() != null) {
            cognitoClient.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .permanent(true)
                    .build());
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
