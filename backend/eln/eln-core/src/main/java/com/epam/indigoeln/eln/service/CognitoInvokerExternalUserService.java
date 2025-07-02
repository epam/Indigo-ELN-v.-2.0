package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Alternative
@ApplicationScoped
public class CognitoInvokerExternalUserService implements ExternalUserService {

    @Inject
    SqsClient sqs;
    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "eln.cognito-invoker.queue-url")
    String queueURL;
    @ConfigProperty(name = "eln.cognito-invoker.user-pool-id")
    String userPoolId;

    @SneakyThrows
    public void createUser(UserRequest request) {
        CognitoOperationsMessage message = new CognitoOperationsMessage(
                CognitoOperationType.CREATE_USER,
                userPoolId,
                request.getUsername(),
                request.getPassword()
        );
        sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueURL)
                .messageBody(objectMapper.writeValueAsString(message))
                .build()
        );
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

    @RegisterForReflection
    public record CognitoOperationsMessage (
            CognitoOperationType type,
            String userPoolId,
            String username,
            @Nullable
            String password
    ) {}

    @RegisterForReflection
    public enum CognitoOperationType {

        CREATE_USER
    }
}
