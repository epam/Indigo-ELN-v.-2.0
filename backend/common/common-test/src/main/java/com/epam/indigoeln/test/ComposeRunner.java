package com.epam.indigoeln.test;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ComposeRunner {

    private final AtomicBoolean stopped = new AtomicBoolean();
    private final ComposeContainer container;
    private final String[] services;

    public ComposeRunner(File composeFile, String... services) {
        ComposeContainer container = new ComposeContainer(composeFile)
                .withBuild(true)
                .withPull(false)
                .withRemoveVolumes(true)
                .withTailChildContainers(true)
                .withEnv(Map.of(
                        "QUARKUS_PROFILE", "integration-test"
                ));
        for (String service : services) {
            container = container
                    .waitingFor(service, Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(1)))
                    .withLogConsumer(service, event -> {
                        if (!stopped.get() && event.getType() == OutputFrame.OutputType.END) {
                            log.error("Service {} unexpectedly stopped", service);
                            throw new RuntimeException("Service unexpectedly stopped: " + service);
                        }
                    });
        }
        this.container = container;
        this.services = services;
    }

    public void start() {
        try {
            container.start();
            log.info("Docker Compose started");
        } catch (Exception e) {
            for (String service : services) {
                Optional<ContainerState> serviceContainer = container.getContainerByServiceName(service);
                if (serviceContainer.isEmpty()) {
                    log.error("Service {} not found", service);
                } else {
                    Object healthStatus;
                    try {
                        healthStatus = serviceContainer.get().isHealthy();
                    } catch (Exception ex) {
                        healthStatus = "Error: " + ex.getMessage();
                    }
                    log.error("Service {}: isCreated={}, isRunning={}, isHealth={}", service, serviceContainer.get().isCreated(), serviceContainer.get().isRunning(), healthStatus);
                }
            }
            stop();
            throw e;
        }
    }

    public void stop() {
        if (stopped.compareAndSet(false, true)) {
            stopped.set(true);
            log.info("Stopping Docker Compose");
            container.stop();
            log.info("Docker Compose Stopped");
        }
    }
}
