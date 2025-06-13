package com.epam.indigoeln.eln.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class CognitoIntegrationTest {

    private LocalStackContainer localStack;
    private CognitoIdentityProviderClient cognitoClient;

    @BeforeEach
    public void setup() {
        // Start LocalStack container with Custom Service (COGNITO)
        localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                .withEnv("SERVICES", "cognito");  // Manually enable Cognito service
        localStack.start();

        // Configure Cognito client to use LocalStack endpoint
        cognitoClient = CognitoIdentityProviderClient.builder()
                .endpointOverride(URI.create(localStack.getEndpointOverride(LocalStackContainer.Service.API).toString())) // Use API override
                .region(localStack.getRegion())
                .build();
    }

    @AfterEach
    public void teardown() {
        localStack.stop();
    }

    @Test
    public void testCreateUserPool() {
        // Create a Cognito User Pool
        CreateUserPoolResponse userPoolResponse = cognitoClient.createUserPool(CreateUserPoolRequest.builder()
                .poolName("test-user-pool")
                .build());

        assertNotNull(userPoolResponse.userPool());
        System.out.println("User Pool Created with ID: " + userPoolResponse.userPool().id());
    }
}