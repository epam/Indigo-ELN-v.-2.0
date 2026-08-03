package com.epam.indigoeln.flyway.service;


import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

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
    void testSubsequentMigrate() throws Exception {
        // delete records for repeatable migrations to force them to re-run
        try (Connection conn = databasePool.get().getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.execute("DELETE FROM flyway_schema_history WHERE version IS NULL");
            }
        }
        flyway.migrate();
    }
}
