package com.epam.indigoeln.flyway.service;


import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DatabaseInitializationServiceTest extends BaseTest {

    @Inject
    Flyway flyway;

    @Test
    @Order(1)
    void testMigrate() {
        flyway.migrate();
    }

    @Test
    @Order(2)
    void testSubsequentMigrate() {
        flyway.migrate();
    }
}
