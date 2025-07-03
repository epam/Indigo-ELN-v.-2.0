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

    @Transactional
    public void cleanupDatabase() {
        // experiments, notebooks, projects
        em.createNativeQuery("delete from Attachment").executeUpdate();
        em.createNativeQuery("delete from Experiment").executeUpdate();
        em.createNativeQuery("delete from Notebook").executeUpdate();
        em.createNativeQuery("delete from Project").executeUpdate();
        em.createNativeQuery("delete from Template").executeUpdate();
        // samples, compounds
        em.createNativeQuery("delete from Sample").executeUpdate();
        em.createNativeQuery("delete from Compound").executeUpdate();
        // users
        em.createNativeQuery("delete from User_Account where username not in ('admin', 'john', 'willow', 'bart', 'lisa')").executeUpdate();
    }
}
