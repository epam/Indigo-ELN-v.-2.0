package com.epam.indigoeln.eln.service;

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

    public String createUser(String username, String email, String tempPassword) {
        AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .temporaryPassword(tempPassword)
                .userAttributes(
                        AttributeType.builder().name("email").value(email).build(),
                        AttributeType.builder().name("email_verified").value("true").build()
                )
                .build();

        AdminCreateUserResponse response = cognitoClient.adminCreateUser(request);
        return response.user().username();
    }

    public void updateUser(String username, String attributeName, String attributeValue) {
        AdminUpdateUserAttributesRequest request = AdminUpdateUserAttributesRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .userAttributes(
                        AttributeType.builder().name(attributeName).value(attributeValue).build()
                )
                .build();

        cognitoClient.adminUpdateUserAttributes(request);
    }

    public void deleteUser(String username) {
        AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        cognitoClient.adminDeleteUser(request);
    }

    public void disableUser(String username) {
        AdminDisableUserRequest request = AdminDisableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        cognitoClient.adminDisableUser(request);
    }

    public void enableUser(String username) {
        AdminEnableUserRequest request = AdminEnableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        cognitoClient.adminEnableUser(request);
    }

    public AdminGetUserResponse getUser(String username) {
        AdminGetUserRequest request = AdminGetUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        return cognitoClient.adminGetUser(request);
    }

    public void resetPassword(String username) {
        AdminResetUserPasswordRequest request = AdminResetUserPasswordRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        cognitoClient.adminResetUserPassword(request);
    }
}