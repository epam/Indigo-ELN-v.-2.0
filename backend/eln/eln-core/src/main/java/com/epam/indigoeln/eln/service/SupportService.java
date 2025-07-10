package com.epam.indigoeln.eln.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import java.util.Map;

// no @Transactional
@ApplicationScoped
@SuppressWarnings("SqlWithoutWhere")
public class SupportService {

    @PersistenceContext
    EntityManager em;
    @Inject
    Flyway flyway;

    @SneakyThrows
    public Map<String, String> migrate() {
        MigrateResult result = flyway.migrate();
        return Map.of(
                "migrationsExecuted", Integer.toString(result.migrationsExecuted)
        );
    }
}
