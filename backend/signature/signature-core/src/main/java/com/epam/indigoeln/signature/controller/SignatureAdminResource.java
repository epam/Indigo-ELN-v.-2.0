package com.epam.indigoeln.signature.controller;

import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.signature.api.SignatureAdminAPI;
import com.epam.indigoeln.signature.service.TestSupportService;
import com.epam.indigoeln.signature.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.flywaydb.core.Flyway;
import org.jspecify.annotations.Nullable;

@Path(SignatureAdminAPI.BASE_PATH)
public class SignatureAdminResource implements SignatureAdminAPI {

    @Inject
    UserService userService;

    @Inject
    TestSupportService testSupportService;

    @Inject
    Flyway flyway;

    @Override
    public void migrate() {
        flyway.migrate();
    }

    @Override
    public UserRef getOrCreateUser(String username, @Nullable String firstName, @Nullable String lastName) {
        return userService.getOrCreateUser(username, firstName, lastName).toRef();
    }

    @Override
    public void cleanupDatabase() {
        testSupportService.cleanupDatabase();
    }
}
