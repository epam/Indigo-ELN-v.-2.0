package com.epam.indigoeln.integrationtests.service;

import com.epam.indigoeln.test.ComposeRunner;
import com.epam.indigoeln.test.SharedTestEnvironment;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;

public class ServiceIntegrationEnvironmentResource implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        SharedTestEnvironment.install(context, Impl::new);
    }

    static class Impl extends SharedTestEnvironment {

        final ComposeRunner composeRunner;

        Impl() {
            composeRunner = new ComposeRunner(new File("../../../deployment-compose/docker-compose.yml"), "postgres", "frontend", "keycloak-healthcheck", "nginx", "eln-service", "reports-service", "signature-service", "sampleregistration-service");
        }

        @Override
        protected void start() {
            composeRunner.start();
        }

        @Override
        protected void stop() {
            composeRunner.stop();
        }
    }
}
