package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

@ApplicationScoped
public class CognitoService {

    @Inject
    CognitoIdentityProviderClient cognitoClient;

    @ConfigProperty(name = "aws.cognito.user-pool-id")
    String userPoolId;

    public String createUser(UserEntity user) {
        var request = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(user.getUsername())
                .userAttributes(
                        AttributeType.builder().name("email").value(user.getUsername()).build(),
                        AttributeType.builder().name("email_verified").value("true").build()
                )
                .build();

        var response = cognitoClient.adminCreateUser(request);
        return response.user().username();
    }

    public void updateUser(String email, String attributeName, String attributeValue) {
        var request = AdminUpdateUserAttributesRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .userAttributes(
                        AttributeType.builder().name(attributeName).value(attributeValue).build()
                )
                .build();

        cognitoClient.adminUpdateUserAttributes(request);
    }

    public void deleteUser(String email) {
        var request = AdminDeleteUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .build();

        cognitoClient.adminDeleteUser(request);
    }

    public void disableUser(String email) {
        var request = AdminDisableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .build();

        cognitoClient.adminDisableUser(request);
    }

    public void enableUser(String email) {
        var request = AdminEnableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .build();

        cognitoClient.adminEnableUser(request);
    }

    public AdminGetUserResponse getUser(String email) {
        var request = AdminGetUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .build();

        return cognitoClient.adminGetUser(request);
    }

    public void resetPassword(String email) {
        var request = AdminResetUserPasswordRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .build();

        cognitoClient.adminResetUserPassword(request);
    }
}