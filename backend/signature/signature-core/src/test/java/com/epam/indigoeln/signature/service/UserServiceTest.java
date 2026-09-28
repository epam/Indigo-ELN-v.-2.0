package com.epam.indigoeln.signature.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @Inject
    UserService userService;

    @Test
    @TestSecurity(user = "john")
    void testGetCurrentUser() {
        var user = userService.getCurrentUser();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getDisplayName()).isEqualTo("John Doe");
    }
}
