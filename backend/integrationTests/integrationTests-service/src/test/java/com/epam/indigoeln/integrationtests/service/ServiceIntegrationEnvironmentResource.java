package com.epam.indigoeln.integrationtests.service;

import com.epam.indigoeln.test.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.util.Map;

public class ServiceIntegrationEnvironmentResource implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ServiceIntegrationEnvironmentResource.class);
    private static final String RESOURCE = "resource";

    @Override
    public void beforeAll(ExtensionContext context) {
        BaseTest.setIntegrationTest(true);
        ResourceImpl resource = (ResourceImpl) context.getRoot().getStore(NAMESPACE).computeIfAbsent(RESOURCE, _ -> new ResourceImpl());
        if (resource.failed) {
            throw new TestAbortedException("Integration environment not started");
        }
        if (!resource.started) {
            try {
                resource.start();
            } catch (Exception e) {
                throw new RuntimeException("Failed to start integration environment", e);
            }
        }
    }
}

@Slf4j
class ResourceImpl implements AutoCloseable {

    private final ComposeContainer composeContainer;
    boolean started;
    boolean failed;
    boolean stopped;

    ResourceImpl() {
        log.info("Starting integration environment via Docker Compose");

        composeContainer = new ComposeContainer(new File("../../../deployment-compose/docker-compose.yml"))
                .withBuild(true)
                .withEnv(Map.of(
                        "QUARKUS_PROFILE", "integration-test"
                ))
                .withRemoveVolumes(true)
                .waitingFor("eln-service", Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(5)))
                .waitingFor("reports-service", Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(5)))
                .waitingFor("signature-service", Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(5)))
                .withTailChildContainers(true);
    }

    void start() {
        try {
            composeContainer.start();
            started = true;
            log.info("Integration environment started");
        } catch (Exception e) {
            failed = true;
            close();
            throw e;
        }
    }

    @Override
    public void close() {
        if (!stopped) {
            stopped = true;
            log.info("Stopping integration environment");
            composeContainer.stop();
            log.info("Integration environment stopped");
        }
    }
}
