package com.epam.indigoeln.integrationtests.service;

import com.epam.indigoeln.test.BaseTest;
import com.epam.indigoeln.test.FeignUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

public class ServiceIntegrationEnvironmentResource implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ServiceIntegrationEnvironmentResource.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        BaseTest.setIntegrationTest(true);
        FeignUtil.setApiSecret("integrationTestsAPISecret");
        context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(
                "integration-environment-resource",
                key -> {
                    try {
                        return new ResourceImpl();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to start integration environment: " + e.getMessage(), e);
                    }
                }
        );
    }
}

@Slf4j
class ResourceImpl implements AutoCloseable {

    private final ComposeContainer composeContainer;

    ResourceImpl() {
        log.info("Starting integration environment via Docker Compose");

        composeContainer = new ComposeContainer(new File("compose-integrationTests-services.yml"))
                .withBuild(true)
                .withLocalCompose(true)
                .waitingFor("eln-service", Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(5)))
                .withTailChildContainers(true);

        composeContainer.start();

        log.info("Integration environment started");
    }

    @Override
    public void close() {
        log.info("Stopping integration environment");
        composeContainer.stop();
        log.info("Integration environment stopped");
    }
}
