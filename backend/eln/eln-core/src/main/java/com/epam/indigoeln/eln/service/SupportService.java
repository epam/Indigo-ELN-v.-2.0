package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.ApplicationPermission;
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

    @Inject
    Flyway flyway;
    @Inject
    ACLService aclService;

    @SneakyThrows
    public Map<String, String> migrate() {
        // don't check permissions if flyway was never applied, as there are no user/role tables yet
        boolean flywayExists = flyway.info().current() != null;
        if (flywayExists) {
            aclService.ensureTopLevelAccess(ApplicationPermission.SYSTEM_OPERATIONS);
        }
        MigrateResult result = flyway.migrate();
        return Map.of(
                "migrationsExecuted", Integer.toString(result.migrationsExecuted)
        );
    }
}
