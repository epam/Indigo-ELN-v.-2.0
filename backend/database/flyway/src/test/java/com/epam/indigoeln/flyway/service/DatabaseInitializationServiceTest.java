package com.epam.indigoeln.flyway.service;


import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DatabaseInitializationServiceTest extends BaseTest {

    @Inject
    DatabaseInitializationService databaseInitializationService;

    @Test
    @Order(1)
    void testMigrate() {
        databaseInitializationService.migrate();
    }

    @Test
    @Order(2)
    void testSubsequentMigrate() {
        databaseInitializationService.migrate();
    }
}
