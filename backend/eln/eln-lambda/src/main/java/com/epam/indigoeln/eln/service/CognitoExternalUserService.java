package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.UserRequest;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

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

    static class Provider {

        @Produces
        ExternalUserService getExternalUserService(Instance<CognitoIdentityProviderClient> cognitoClient) {
            if (ConfigUtils.isProfileActive("devtest")) {
                return new FakeExternalServiceImpl();
            }
            return new CognitoExternalUserService(cognitoClient.get(), ConfigProvider.getConfig().getValue("eln.cognito.user-pool-id", String.class));
        }
    }
}
