package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.UserRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.swing.text.html.Option;
import java.util.Optional;

@Alternative
@ApplicationScoped
class CognitoExternalUserService implements ExternalUserService {

    private final CognitoIdentityProviderClient cognitoClient;

    private final String userPoolId;

    @Inject
    public CognitoExternalUserService(CognitoIdentityProviderClient cognitoClient
            , @ConfigProperty(name = "eln.cognito.user-pool-id") Optional<String> userPoolId
            , @ConfigProperty(name = "eln.cognito.user-pool-name") Optional<String> userPoolName // used in tests where userPoolId is not known beforehand
    ) {
        this.cognitoClient = cognitoClient;
        if (userPoolName.isPresent()) {
            ListUserPoolsResponse existingPools = cognitoClient.listUserPools(ListUserPoolsRequest.builder().build());
            UserPoolDescriptionType userPool = existingPools.userPools().stream()
                    .filter(pool -> pool.name().equals(userPoolName.get()))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("Cognito user pool not found: " + userPoolName));
            this.userPoolId = userPool.id();
        } else if (userPoolId.isPresent()) {
            this.userPoolId = userPoolId.get();
        } else {
            throw new IllegalStateException("Cognito user pool ID or name must be provided");
        }
    }

    @Override
    public void createUser(UserRequest request) {
        try {
            cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(request.getUsername())
                    .userAttributes(
                            AttributeType.builder().name("email").value(request.getUsername()).build()
//                        AttributeType.builder().name("email_verified").value("true").build()
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
