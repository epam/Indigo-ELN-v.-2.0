package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.service.CognitoService;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import software.amazon.awssdk.regions.Region;

import javax.inject.Inject;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CognitoIntegrationTest {

    LocalStackContainer localStack;
    CognitoIdentityProviderClient cognitoClient;
    @Inject
    CognitoService cognitoService; // Properly injected by Quarkus

    @BeforeEach
    public void setup() {
        // Start LocalStack container
        localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                .withEnv("SERVICES", "cognito");
        localStack.start();

        // Configure Cognito client
        cognitoClient = CognitoIdentityProviderClient.builder()
                .endpointOverride(URI.create(localStack.getEndpointOverride(LocalStackContainer.Service.API_GATEWAY).toString()))
                .region(Region.of(localStack.getRegion()))
                .build();

        // Register CognitoClient with Quarkus for injection
        QuarkusMock.installMockForType(cognitoClient, CognitoIdentityProviderClient.class);
    }

    @AfterEach
    public void teardown() {
        localStack.stop();
    }

    @Test
    public void testCreateUser() {
        var user = new UserEntity();
        user.setUsername("testuser@example.com");

        var createdUsername = cognitoService.createUser(user);

        assertEquals(user.getUsername(), createdUsername);

        var retrievedUser = cognitoService.getUser(user.getUsername());
        assertEquals(user.getUsername(), retrievedUser.username());
        assertEquals("testuser@example.com", retrievedUser.userAttributes().stream()
                .filter(attr -> attr.name().equals("email"))
                .findFirst()
                .get().value());
    }

    @Test
    public void testUpdateUser() {
        var user = new UserEntity();
        user.setUsername("updateuser@example.com");
        cognitoService.createUser(user);

        cognitoService.updateUser(user.getUsername(), "custom:role", "admin");

        var updatedUser = cognitoService.getUser(user.getUsername());
        assertEquals("admin", updatedUser.userAttributes().stream()
                .filter(attr -> attr.name().equals("custom:role"))
                .findFirst()
                .get().value());
    }

    @Test
    public void testDeleteUser() {
        var user = new UserEntity();
        user.setUsername("deleteuser@example.com");
        cognitoService.createUser(user);
        cognitoService.deleteUser(user.getUsername());

        assertThrows(UserNotFoundException.class, () -> cognitoService.getUser(user.getUsername()));
    }

    @Test
    public void testDisableUserAndEnableUser() {
        var user = new UserEntity();
        user.setUsername("disableuser@example.com");
        cognitoService.createUser(user);

        cognitoService.disableUser(user.getUsername());
        var disabledUserResponse = cognitoService.getUser(user.getUsername());
        assertFalse(disabledUserResponse.enabled());

        cognitoService.enableUser(user.getUsername());
        var enabledUserResponse = cognitoService.getUser(user.getUsername());
        assertTrue(enabledUserResponse.enabled());
    }

    @Test
    public void testResetPassword() {
        var user = new UserEntity();
        user.setUsername("resetpassworduser@example.com");
        cognitoService.createUser(user);

        assertDoesNotThrow(() -> cognitoService.resetPassword(user.getUsername()));
    }

    @Test
    public void testGetUser() {
        var user = new UserEntity();
        user.setUsername("getuser@example.com");
        cognitoService.createUser(user);

        var userResponse = cognitoService.getUser(user.getUsername());

        assertNotNull(userResponse);
        assertEquals(user.getUsername(), userResponse.username());
    }
}