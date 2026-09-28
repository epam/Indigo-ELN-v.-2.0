package com.epam.indigoeln.flyway.service;


import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class DatabaseInitializationServiceTest extends BaseTest {

    @Inject
    Flyway flyway;

    @Test
    @Order(1)
    void testMigrate() {
        MigrateResult result = flyway.migrate();
        assertThat(result.migrationsExecuted).isPositive();
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
        MigrateResult result = flyway.migrate();
        assertThat(result.migrationsExecuted).isPositive();
    }
}
